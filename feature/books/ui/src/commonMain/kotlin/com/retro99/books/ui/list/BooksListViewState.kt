package com.retro99.books.ui.list

import com.retro99.books.domain.model.BookDomainModel

data class BooksListViewState(
    val books: List<BookDomainModel> = emptyList(),
    val isRefreshing: Boolean = false,
)

