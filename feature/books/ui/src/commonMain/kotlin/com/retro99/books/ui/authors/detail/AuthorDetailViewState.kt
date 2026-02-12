package com.retro99.books.ui.authors.detail

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel

data class AuthorDetailViewState(
    val authorUuid: String = "",
    val authorName: String = "",
    val books: List<BookUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
)

