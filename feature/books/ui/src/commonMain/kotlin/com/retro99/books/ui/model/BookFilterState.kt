package com.retro99.books.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class BookFilterState(
    val activeQuickFilters: Set<BookQuickFilter> = emptySet(),
) {
    val hasActiveFilters: Boolean
        get() = activeQuickFilters.isNotEmpty()

    val activeFilterCount: Int
        get() = activeQuickFilters.size
}
