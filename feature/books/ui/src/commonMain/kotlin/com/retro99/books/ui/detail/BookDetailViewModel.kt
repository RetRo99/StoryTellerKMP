package com.retro99.books.ui.detail

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.log
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import com.retro99.books.ui.model.toUiModel
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadState
import com.retro99.reader.domain.usecase.CancelDownloadUseCase
import com.retro99.reader.domain.usecase.DeleteMediaCacheUseCase
import com.retro99.reader.domain.usecase.DownloadMediaUseCase
import com.retro99.reader.domain.usecase.ObserveDownloadStateUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class BookDetailViewModel(
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val onNavigateToReader: (bookUuid: String, bookType: BookType) -> Unit,
    @InjectedParam private val onBack: () -> Unit,
    @Provided private val getBookByUuidUseCase: GetBookByUuidUseCase,
    @Provided private val downloadMediaUseCase: DownloadMediaUseCase,
    @Provided private val cancelDownloadUseCase: CancelDownloadUseCase,
    @Provided private val observeDownloadStateUseCase: ObserveDownloadStateUseCase,
    @Provided private val deleteMediaCacheUseCase: DeleteMediaCacheUseCase,
    @Provided private val analytics: Analytics,
) : BaseViewModel<BookDetailViewState, BookDetailIntent>(
    BookDetailViewState(),
) {

    init {
        fetchBook()
        observeDownloadStates()
    }

    override fun onIntent(intent: BookDetailIntent) {
        when (intent) {
            BookDetailIntent.OnBackClicked -> {
                onBack()
            }

            BookDetailIntent.OnRetryClicked -> {
                fetchBook()
            }

            is BookDetailIntent.OnTagClicked -> {
                // TODO: Navigate to tag filter
            }

            is BookDetailIntent.OnSeriesClicked -> {
                // TODO: Navigate to series
            }

            BookDetailIntent.OnReadEbookClicked -> {
                handleReadClick(BookType.EBOOK)
            }

            BookDetailIntent.OnPlayAudiobookClicked -> {
                handleReadClick(BookType.AUDIOBOOK)
            }

            BookDetailIntent.OnReadReadaloudClicked -> {
                handleReadClick(BookType.READALOUD)
            }

            is BookDetailIntent.OnDownloadClicked -> {
                toggleDownload(intent.bookType)
            }

            is BookDetailIntent.OnDeleteCacheClicked -> {
                updateState { it.copy(deleteConfirmationBookType = intent.bookType) }
            }

            BookDetailIntent.OnDeleteCacheConfirmed -> {
                viewState.value.deleteConfirmationBookType?.let { bookType ->
                    deleteMediaCache(bookType)
                }
                updateState { it.copy(deleteConfirmationBookType = null) }
            }

            BookDetailIntent.OnDeleteCacheDismissed -> {
                updateState { it.copy(deleteConfirmationBookType = null) }
            }
        }
    }

    private fun handleReadClick(bookType: BookType) {
        val currentState = viewState.value
        val downloadState = when (bookType) {
            BookType.EBOOK -> currentState.ebookDownloadState
            BookType.AUDIOBOOK -> currentState.audiobookDownloadState
            BookType.READALOUD -> currentState.readaloudDownloadState
        }

        if (downloadState is DownloadState.Cached) {
            onNavigateToReader(bookUuid, bookType)
        }
        // If not cached, user should click download first
    }

    private fun fetchBook() {
        getBookByUuidUseCase(bookUuid)
            .onStart {
                updateState { it.copy(isLoading = true, error = null) }
            }
            .onEach { result ->
                result
                    .onSuccess { book ->
                        updateState {
                            it.copy(
                                book = book.toUiModel(),
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        error.log(analytics, "BookDetailViewModel: Failed to load book details")
                        updateState {
                            it.copy(
                                isLoading = false,
                                error = error,
                            )
                        }
                    }
            }
            .launchIn(viewModelScope)
    }

    private fun observeDownloadStates() {
        combine(
            observeDownloadStateUseCase(bookUuid, BookType.EBOOK),
            observeDownloadStateUseCase(bookUuid, BookType.AUDIOBOOK),
            observeDownloadStateUseCase(bookUuid, BookType.READALOUD),
        ) { ebookState, audiobookState, readaloudState ->
            Triple(ebookState, audiobookState, readaloudState)
        }
            .onEach { (ebookState, audiobookState, readaloudState) ->
                updateState {
                    it.copy(
                        ebookDownloadState = ebookState,
                        audiobookDownloadState = audiobookState,
                        readaloudDownloadState = readaloudState,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun toggleDownload(bookType: BookType) {
        val book = viewState.value.book ?: return
        val currentState = when (bookType) {
            BookType.EBOOK -> viewState.value.ebookDownloadState
            BookType.AUDIOBOOK -> viewState.value.audiobookDownloadState
            BookType.READALOUD -> viewState.value.readaloudDownloadState
        }

        // If currently downloading, cancel it
        if (currentState is DownloadState.Downloading) {
            viewModelScope.launch {
                cancelDownloadUseCase(bookUuid, bookType)
            }
            return
        }

        // Otherwise, start the download
        val filePath = when (bookType) {
            BookType.EBOOK -> book.ebookFilepath
            BookType.AUDIOBOOK -> book.audiobookFilepath
            BookType.READALOUD -> book.readaloudFilepath
        } ?: return

        viewModelScope.launch {
            downloadMediaUseCase(bookUuid, bookType, filePath, book.title)
        }
    }

    private fun deleteMediaCache(bookType: BookType) {
        viewModelScope.launch {
            deleteMediaCacheUseCase(bookUuid, bookType)
            // Download state will be updated via the observer
        }
    }
}

