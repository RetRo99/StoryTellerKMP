package com.retro99.books.ui.list

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel

data class BooksListViewState(
    val books: List<BookUiModel> = emptyList(),
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val favoriteBookUuids: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
) {
    val filteredBooks: List<BookUiModel>
        get() = if (searchQuery.isBlank()) {
            books
        } else {
            val query = searchQuery.lowercase()
            books.filter { book ->
                book.title.lowercase().contains(query) ||
                        book.authors.any { it.lowercase().contains(query) } ||
                        book.series.any { it.name.lowercase().contains(query) } ||
                        book.tags.any { it.lowercase().contains(query) }
            }
        }
}

