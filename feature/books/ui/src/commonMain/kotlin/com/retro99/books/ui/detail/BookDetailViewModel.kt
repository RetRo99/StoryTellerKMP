package com.retro99.books.ui.detail

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import com.retro99.books.ui.model.toUiModel
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadStateDomainModel
import com.retro99.reader.domain.usecase.CancelDownloadUseCase
import com.retro99.reader.domain.usecase.DeleteMediaCacheUseCase
import com.retro99.reader.domain.usecase.DownloadMediaUseCase
import com.retro99.reader.domain.usecase.ObserveDownloadStateUseCase
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
    @Provided private val getBookByUuidUseCase: GetBookByUuidUseCase,
    @Provided private val downloadMediaUseCase: DownloadMediaUseCase,
    @Provided private val cancelDownloadUseCase: CancelDownloadUseCase,
    @Provided private val observeDownloadStateUseCase: ObserveDownloadStateUseCase,
    @Provided private val deleteMediaCacheUseCase: DeleteMediaCacheUseCase,
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
                // Navigation is handled by the parent navigation component
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

        if (downloadState is DownloadStateDomainModel.Cached) {
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
        observeDownloadStateUseCase(bookUuid, BookType.EBOOK)
            .onEach { state ->
                updateState { it.copy(ebookDownloadState = state) }
            }
            .launchIn(viewModelScope)

        observeDownloadStateUseCase(bookUuid, BookType.AUDIOBOOK)
            .onEach { state ->
                updateState { it.copy(audiobookDownloadState = state) }
            }
            .launchIn(viewModelScope)

        observeDownloadStateUseCase(bookUuid, BookType.READALOUD)
            .onEach { state ->
                updateState { it.copy(readaloudDownloadState = state) }
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
        if (currentState is DownloadStateDomainModel.Downloading) {
            cancelDownloadUseCase(bookUuid, bookType)
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

