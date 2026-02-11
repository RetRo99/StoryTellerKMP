package com.retro99.books.ui.detail

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import com.retro99.books.ui.model.toUiModel
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.usecase.GetMediaCacheStatusUseCase
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
    @Provided private val getMediaCacheStatusUseCase: GetMediaCacheStatusUseCase,
) : BaseViewModel<BookDetailViewState, BookDetailIntent>(
    BookDetailViewState(),
) {

    init {
        fetchBook()
        checkMediaCacheStatus()
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
                onNavigateToReader(bookUuid, BookType.EBOOK)
            }

            BookDetailIntent.OnPlayAudiobookClicked -> {
                // TODO: Open audiobook player
            }

            BookDetailIntent.OnReadReadaloudClicked -> {
                onNavigateToReader(bookUuid, BookType.READALOUD)
            }
        }
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

    private fun checkMediaCacheStatus() {
        viewModelScope.launch {
            val cacheStatus = getMediaCacheStatusUseCase(bookUuid)
            updateState {
                it.copy(
                    isEbookCached = cacheStatus.isEbookCached,
                    isAudiobookCached = cacheStatus.isAudiobookCached,
                    isReadaloudCached = cacheStatus.isReadaloudCached,
                )
            }
        }
    }
}

