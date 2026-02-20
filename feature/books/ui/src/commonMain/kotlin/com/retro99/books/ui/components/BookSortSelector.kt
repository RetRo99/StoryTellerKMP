package com.retro99.books.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retro99.books.ui.model.BookSortConfig
import com.retro99.books.ui.model.BookSortOption
import com.retro99.books.ui.model.SortDirection
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import resources.translations.books_sort_a_to_z
import resources.translations.books_sort_author
import resources.translations.books_sort_date_added
import resources.translations.books_sort_highest
import resources.translations.books_sort_label
import resources.translations.books_sort_lowest
import resources.translations.books_sort_newest
import resources.translations.books_sort_oldest
import resources.translations.books_sort_rating
import resources.translations.books_sort_title
import resources.translations.books_sort_z_to_a

/**
 * Sort selector with dropdown for sort option and segmented button for direction.
 */
@Composable
fun BookSortSelector(
    sortConfig: BookSortConfig,
    onSortChanged: (BookSortConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Sort,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SuggestionChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = "${stringResource(StringRes.books_sort_label)}: ${stringResource(sortConfig.option.labelRes)}"
                )
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            BookSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        onSortChanged(sortConfig.copy(option = option))
                        expanded = false
                    },
                )
            }
        }

        // Segmented button for sort direction with contextual labels
        SortDirectionSelector(
            sortOption = sortConfig.option,
            direction = sortConfig.direction,
            onDirectionChanged = { newDirection ->
                onSortChanged(sortConfig.copy(direction = newDirection))
            },
        )
    }
}

/**
 * Filter chips for selecting sort direction with contextual labels.
 * Labels change based on the sort option (e.g., "A→Z" for title, "Newest" for date).
 */
@Composable
private fun SortDirectionSelector(
    sortOption: BookSortOption,
    direction: SortDirection,
    onDirectionChanged: (SortDirection) -> Unit,
) {
    val (ascendingLabel, descendingLabel) = sortOption.directionLabels

    FilterChip(
        selected = direction == SortDirection.ASCENDING,
        onClick = { onDirectionChanged(SortDirection.ASCENDING) },
        label = { Text(text = stringResource(ascendingLabel)) },
    )
    FilterChip(
        selected = direction == SortDirection.DESCENDING,
        onClick = { onDirectionChanged(SortDirection.DESCENDING) },
        label = { Text(text = stringResource(descendingLabel)) },
    )
}

/**
 * String resource for each sort option.
 */
private val BookSortOption.labelRes: StringResource
    get() = when (this) {
        BookSortOption.TITLE -> StringRes.books_sort_title
        BookSortOption.AUTHOR -> StringRes.books_sort_author
        BookSortOption.RATING -> StringRes.books_sort_rating
        BookSortOption.DATE_ADDED -> StringRes.books_sort_date_added
    }

/**
 * Direction labels for each sort option.
 * Returns a pair of (ascending label, descending label).
 */
private val BookSortOption.directionLabels: Pair<StringResource, StringResource>
    get() = when (this) {
        BookSortOption.TITLE -> StringRes.books_sort_a_to_z to StringRes.books_sort_z_to_a
        BookSortOption.AUTHOR -> StringRes.books_sort_a_to_z to StringRes.books_sort_z_to_a
        BookSortOption.RATING -> StringRes.books_sort_highest to StringRes.books_sort_lowest
        BookSortOption.DATE_ADDED -> StringRes.books_sort_oldest to StringRes.books_sort_newest
    }

