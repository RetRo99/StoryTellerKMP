package com.retro99.books.ui.list

import com.retro99.base.ui.BaseIntent
import com.retro99.books.ui.model.BookQuickFilter
import com.retro99.books.ui.model.BookSortConfig
import com.retro99.books.ui.model.BookUiModel
import io.github.vinceglb.filekit.core.PlatformFile

sealed interface BooksListIntent : BaseIntent {
    data object OnRefresh : BooksListIntent
    data object OnSearchToggled : BooksListIntent
    data object OnFilterToggled : BooksListIntent
    data class OnBookClicked(val book: BookUiModel) : BooksListIntent
    data class OnFavoriteClicked(val bookUuid: String) : BooksListIntent
    data class OnImportBook(val file: PlatformFile) : BooksListIntent

    // Filter intents
    data class OnQuickFilterToggled(val filter: BookQuickFilter) : BooksListIntent
    data object OnClearAllFilters : BooksListIntent

    // Sort intents
    data class OnSortChanged(val sortConfig: BookSortConfig) : BooksListIntent
}

