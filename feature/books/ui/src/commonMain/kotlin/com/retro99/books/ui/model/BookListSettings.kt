package com.retro99.books.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class BookListSettings(
    val filterState: BookFilterState = BookFilterState(),
    val sortConfig: BookSortConfig = BookSortConfig(),
    val viewMode: BookListViewMode = BookListViewMode.LIST,
)
