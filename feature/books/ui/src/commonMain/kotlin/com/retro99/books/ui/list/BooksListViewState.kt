package com.retro99.books.ui.list

import com.retro99.base.result.AppError
import com.retro99.books.domain.model.BookDomainModel

data class BooksListViewState(
    val books: List<BookDomainModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
)

