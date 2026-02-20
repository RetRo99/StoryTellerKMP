package com.retro99.books.ui.model

import kotlinx.serialization.Serializable

/**
 * Available sorting options for the book list.
 */
@Serializable
enum class BookSortOption {
    TITLE,
    AUTHOR,
    RATING,
    DATE_ADDED,
}

/**
 * Sort direction.
 */
@Serializable
enum class SortDirection {
    ASCENDING,
    DESCENDING,
}

/**
 * Complete sort configuration.
 */
@Serializable
data class BookSortConfig(
    val option: BookSortOption = BookSortOption.TITLE,
    val direction: SortDirection = SortDirection.ASCENDING,
)

