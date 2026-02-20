package com.retro99.books.ui.model

/**
 * Available sorting options for the book list.
 */
enum class BookSortOption {
    TITLE,
    AUTHOR,
    RATING,
    DATE_ADDED,
}

/**
 * Sort direction.
 */
enum class SortDirection {
    ASCENDING,
    DESCENDING,
}

/**
 * Complete sort configuration.
 */
data class BookSortConfig(
    val option: BookSortOption = BookSortOption.TITLE,
    val direction: SortDirection = SortDirection.ASCENDING,
)

