package com.retro99.books.ui.authors.detail

import com.retro99.base.ui.BaseIntent
import com.retro99.books.ui.model.BookUiModel

sealed interface AuthorDetailIntent : BaseIntent {
    data object OnBackClicked : AuthorDetailIntent
    data object OnRefresh : AuthorDetailIntent
    data class OnBookClicked(val book: BookUiModel) : AuthorDetailIntent
}

