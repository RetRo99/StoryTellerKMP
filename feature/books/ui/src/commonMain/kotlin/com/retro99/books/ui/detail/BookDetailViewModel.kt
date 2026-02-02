package com.retro99.books.ui.detail

import com.retro99.base.ui.BaseViewModel
import com.retro99.books.ui.model.BookUiModel
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class BookDetailViewModel(
    @InjectedParam private val book: BookUiModel,
    @InjectedParam private val onNavigateToReader: (
        bookUuid: String,
        ebookFilePath: String,
        initialLocatorHref: String?,
        initialLocatorType: String?,
        initialLocatorProgression: Double?,
        initialLocatorPosition: Int?,
        initialLocatorTotalProgression: Double?,
    ) -> Unit,
) : BaseViewModel<BookDetailViewState, BookDetailIntent>(
    BookDetailViewState(book = book, isLoading = false),
) {

    override fun onIntent(intent: BookDetailIntent) {
        when (intent) {
            BookDetailIntent.OnBackClicked -> {
                // Navigation is handled by the parent navigation component
            }

            BookDetailIntent.OnRetryClicked -> {
                // No longer needed since book is passed directly
            }

            is BookDetailIntent.OnTagClicked -> {
                // TODO: Navigate to tag filter
            }

            is BookDetailIntent.OnSeriesClicked -> {
                // TODO: Navigate to series
            }

            BookDetailIntent.OnReadEbookClicked -> {
                val currentBook = currentViewState().book ?: return
                val ebookFilepath = currentBook.ebookFilepath ?: return
                onNavigateToReader(
                    currentBook.uuid,
                    ebookFilepath,
                    currentBook.locator?.href,
                    currentBook.locator?.type,
                    currentBook.locator?.progression,
                    currentBook.locator?.position,
                    currentBook.locator?.totalProgression,
                )
            }

            BookDetailIntent.OnPlayAudiobookClicked -> {
                // TODO: Open audiobook player
            }
        }
    }
}

