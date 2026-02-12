package com.retro99.books.ui.series.detail

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel

data class SeriesDetailViewState(
    val seriesUuid: String = "",
    val seriesName: String = "",
    val books: List<BookUiModel> = emptyList(),
    val favoriteBookUuids: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
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
                        book.authors.any { it.lowercase().contains(query) }
            }
        }
}

