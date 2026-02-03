package com.retro99.books.ui.detail

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import com.retro99.books.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class BookDetailViewModel(
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val onNavigateToReader: (bookUuid: String) -> Unit,
    @Provided private val getBookByUuidUseCase: GetBookByUuidUseCase,
) : BaseViewModel<BookDetailViewState, BookDetailIntent>(
    BookDetailViewState(),
) {

    init {
        fetchBook()
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
                onNavigateToReader(bookUuid)
            }

            BookDetailIntent.OnPlayAudiobookClicked -> {
                // TODO: Open audiobook player
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
}

