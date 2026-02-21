package com.retro99.books.ui.list

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookFilterState
import com.retro99.books.ui.model.BookProgressInfoUiModel
import com.retro99.books.ui.model.BookQuickFilter
import com.retro99.books.ui.model.BookSortConfig
import com.retro99.books.ui.model.BookSortOption
import com.retro99.books.ui.model.BookUiModel
import com.retro99.books.ui.model.SortDirection

data class BooksListViewState(
    val books: List<BookUiModel> = emptyList(),
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val isFilterVisible: Boolean = false,
    val favoriteBookUuids: Set<String> = emptySet(),
    val bookProgressInfo: Map<String, BookProgressInfoUiModel> = emptyMap(),
    val filterState: BookFilterState = BookFilterState(),
    val sortConfig: BookSortConfig = BookSortConfig(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isImporting: Boolean = false,
    val error: AppError? = null,
) {
    val filteredBooks: List<BookUiModel>
        get() = books
            .applySearchFilter(searchQuery)
            .applyQuickFilters(filterState.activeQuickFilters, favoriteBookUuids)
            .applySorting(sortConfig)

    private fun List<BookUiModel>.applySearchFilter(query: String): List<BookUiModel> {
        if (query.isBlank()) return this
        val lowerQuery = query.lowercase()
        return filter { book ->
            book.title.lowercase().contains(lowerQuery) ||
                    book.authors.any { it.lowercase().contains(lowerQuery) } ||
                    book.series.any { it.name.lowercase().contains(lowerQuery) } ||
                    book.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }

    private fun List<BookUiModel>.applyQuickFilters(
        filters: Set<BookQuickFilter>,
        favoriteUuids: Set<String>,
    ): List<BookUiModel> {
        if (filters.isEmpty()) return this
        return filter { book ->
            filters.all { filter ->
                when (filter) {
                    BookQuickFilter.FAVORITES -> book.uuid in favoriteUuids
                    BookQuickFilter.HAS_EBOOK -> book.hasEbook
                    BookQuickFilter.HAS_READALOUD -> book.hasReadaloud
                    BookQuickFilter.IN_SERIES -> book.series.isNotEmpty()
                    BookQuickFilter.LOCAL_BOOKS -> book is BookUiModel.LocalBook
                    BookQuickFilter.REMOTE_BOOKS -> book is BookUiModel.StorytellerBook
                }
            }
        }
    }

    private fun List<BookUiModel>.applySorting(config: BookSortConfig): List<BookUiModel> {
        val comparator: Comparator<BookUiModel> = when (config.option) {
            BookSortOption.TITLE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            BookSortOption.AUTHOR -> compareBy(String.CASE_INSENSITIVE_ORDER) {
                it.authors.firstOrNull() ?: ""
            }
            BookSortOption.RATING -> compareBy(nullsLast()) { it.rating }
            BookSortOption.DATE_PUBLISHED -> compareBy(nullsLast()) { it.publicationDate }
            BookSortOption.DATE_ADDED -> compareBy(nullsLast()) { it.dateAdded }
        }
        return when (config.direction) {
            SortDirection.ASCENDING -> sortedWith(comparator)
            SortDirection.DESCENDING -> sortedWith(comparator.reversed())
        }
    }
}

