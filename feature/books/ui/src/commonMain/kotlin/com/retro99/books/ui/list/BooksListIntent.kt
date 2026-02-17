package com.retro99.books.ui.list

import com.retro99.base.ui.BaseIntent
import com.retro99.books.ui.model.BookUiModel
import io.github.vinceglb.filekit.core.PlatformFile

sealed interface BooksListIntent : BaseIntent {
    data object OnRefresh : BooksListIntent
    data object OnSearchToggled : BooksListIntent
    data class OnBookClicked(val book: BookUiModel) : BooksListIntent
    data class OnFavoriteClicked(val bookUuid: String) : BooksListIntent
    data class OnImportBook(val file: PlatformFile) : BooksListIntent
}

