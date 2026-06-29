package com.retro99.books.ui.model

import com.retro99.base.server.ServerType
import kotlinx.serialization.Serializable

@Serializable
data class BookFilterState(
    val activeQuickFilters: Set<BookQuickFilter> = emptySet(),
    val serverTypeFilter: ServerType? = null,
) {
    val hasActiveFilters: Boolean
        get() = activeQuickFilters.isNotEmpty() || serverTypeFilter != null

    val activeFilterCount: Int
        get() = activeQuickFilters.size + (if (serverTypeFilter != null) 1 else 0)
}
