package com.retro99.books.ui.model

/**
 * Complete filter state for the book list.
 * Combines quick filters and advanced selection filters.
 */
data class BookFilterState(
    // Quick toggle filters
    val activeQuickFilters: Set<BookQuickFilter> = emptySet(),

    // Selection filters
    val selectedServerIds: Set<String> = emptySet(),
    val selectedAuthors: Set<String> = emptySet(),
    val selectedSeries: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),

    // Rating range filter (null means no filter)
    val minRating: Float? = null,
    val maxRating: Float? = null,
) {
    /**
     * Returns true if any filter is active.
     */
    val hasActiveFilters: Boolean
        get() = activeQuickFilters.isNotEmpty() ||
                selectedServerIds.isNotEmpty() ||
                selectedAuthors.isNotEmpty() ||
                selectedSeries.isNotEmpty() ||
                selectedTags.isNotEmpty() ||
                minRating != null ||
                maxRating != null

    /**
     * Returns the total count of active filters.
     */
    val activeFilterCount: Int
        get() = activeQuickFilters.size +
                selectedServerIds.size +
                selectedAuthors.size +
                selectedSeries.size +
                selectedTags.size +
                (if (minRating != null || maxRating != null) 1 else 0)
}

