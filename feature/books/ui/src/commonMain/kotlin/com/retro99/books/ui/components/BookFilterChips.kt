package com.retro99.books.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.retro99.books.ui.model.BookQuickFilter
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import resources.translations.books_filter_ebook
import resources.translations.books_filter_favorites
import resources.translations.books_filter_in_series
import resources.translations.books_filter_local
import resources.translations.books_filter_readaloud
import resources.translations.books_filter_remote

/**
 * Horizontal scrollable row of filter chips with animations.
 */
@Composable
fun BookFilterChipsRow(
    activeFilters: Set<BookQuickFilter>,
    onFilterToggle: (BookQuickFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BookQuickFilter.entries.forEach { filter ->
            val isSelected = filter in activeFilters

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                label = "chipScale",
            )

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                label = "chipColor",
            )

            FilterChip(
                selected = isSelected,
                onClick = { onFilterToggle(filter) },
                label = { Text(stringResource(filter.labelRes)) },
                modifier = Modifier.scale(scale),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = containerColor,
                ),
            )
        }
    }
}

/**
 * String resource for each quick filter.
 */
private val BookQuickFilter.labelRes: StringResource
    get() = when (this) {
        BookQuickFilter.FAVORITES -> StringRes.books_filter_favorites
        BookQuickFilter.HAS_EBOOK -> StringRes.books_filter_ebook
        BookQuickFilter.HAS_READALOUD -> StringRes.books_filter_readaloud
        BookQuickFilter.IN_SERIES -> StringRes.books_filter_in_series
        BookQuickFilter.LOCAL_BOOKS -> StringRes.books_filter_local
        BookQuickFilter.REMOTE_BOOKS -> StringRes.books_filter_remote
    }

