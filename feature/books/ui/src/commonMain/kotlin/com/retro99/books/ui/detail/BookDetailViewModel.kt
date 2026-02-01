package com.retro99.books.ui.detail

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class BookDetailViewModel(
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val onNavigateToReader: (bookUuid: String, ebookFilePath: String) -> Unit,
    @Provided private val getBookByUuidUseCase: GetBookByUuidUseCase,
) : BaseViewModel<BookDetailViewState, BookDetailIntent>(BookDetailViewState()) {

    init {
        loadBook(bookUuid)
    }

    override fun onIntent(intent: BookDetailIntent) {
        when (intent) {
            BookDetailIntent.OnBackClicked -> {
                // Navigation is handled by the parent navigation component
            }

            BookDetailIntent.OnRetryClicked -> loadBook(bookUuid)
            is BookDetailIntent.OnTagClicked -> {
                // TODO: Navigate to tag filter
            }

            is BookDetailIntent.OnSeriesClicked -> {
                // TODO: Navigate to series
            }

            BookDetailIntent.OnReadEbookClicked -> {
                currentViewState().book?.ebook?.let { ebook ->
                    onNavigateToReader(bookUuid, ebook.filepath)
                }
            }

            BookDetailIntent.OnPlayAudiobookClicked -> {
                // TODO: Open audiobook player
            }
        }
    }

    private fun loadBook(bookUuid: String) {
        updateState { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getBookByUuidUseCase(bookUuid).fold(
                success = { book ->
                    updateState { it.copy(book = book, isLoading = false, error = null) }
                },
                failure = { error ->
                    updateState { it.copy(isLoading = false, error = error) }
                },
            )
        }
    }
}

