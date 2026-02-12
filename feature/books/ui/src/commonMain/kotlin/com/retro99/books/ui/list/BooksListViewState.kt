package com.retro99.books.ui.list

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel

data class BooksListViewState(
    val books: List<BookUiModel> = emptyList(),
    val favoriteBookUuids: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
)

