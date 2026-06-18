package com.retro99.books.ui.model

import kotlinx.serialization.Serializable

/**
 * Persisted settings for the book list screen.
 * Stored in user preferences to remember filter and sort choices.
 */
@Serializable
data class BookListSettings(
    val filterState: BookFilterState = BookFilterState(),
    val sortConfig: BookSortConfig = BookSortConfig(),
    val viewMode: BookListViewMode = BookListViewMode.LIST,
    val selectedSection: BookLibrarySection = BookLibrarySection.ALL,
)

