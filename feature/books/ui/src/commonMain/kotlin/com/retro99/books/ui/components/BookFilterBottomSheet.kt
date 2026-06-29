package com.retro99.books.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retro99.base.server.ServerType
import com.retro99.books.ui.model.BookFilterState
import com.retro99.books.ui.model.BookQuickFilter
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import resources.translations.books_filter_all
import resources.translations.books_filter_cached
import resources.translations.books_filter_clear_all
import resources.translations.books_filter_ebook
import resources.translations.books_filter_favorites
import resources.translations.books_filter_in_progress
import resources.translations.books_filter_in_series
import resources.translations.books_filter_quick_filters
import resources.translations.books_filter_readaloud
import resources.translations.books_filter_server
import resources.translations.books_filter_sheet_title

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookFilterBottomSheet(
    filterState: BookFilterState,
    onFilterToggle: (BookQuickFilter) -> Unit,
    onServerTypeFilterChanged: (ServerType?) -> Unit,
    onClearAllFilters: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(StringRes.books_filter_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (filterState.hasActiveFilters) {
                    TextButton(onClick = onClearAllFilters) {
                        Text(text = stringResource(StringRes.books_filter_clear_all))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(StringRes.books_filter_server),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filterState.serverTypeFilter == null,
                    onClick = { onServerTypeFilterChanged(null) },
                    label = { Text(stringResource(StringRes.books_filter_all)) },
                )
                ServerType.entries.forEach { serverType ->
                    FilterChip(
                        selected = filterState.serverTypeFilter == serverType,
                        onClick = {
                            onServerTypeFilterChanged(
                                if (filterState.serverTypeFilter == serverType) null else serverType,
                            )
                        },
                        label = { Text(serverType.displayName) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(StringRes.books_filter_quick_filters),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BookQuickFilter.entries.forEach { filter ->
                    val isSelected = filter in filterState.activeQuickFilters
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterToggle(filter) },
                        label = { Text(stringResource(filter.labelRes)) },
                    )
                }
            }
        }
    }
}

private val BookQuickFilter.labelRes: StringResource
    get() = when (this) {
        BookQuickFilter.FAVORITES -> StringRes.books_filter_favorites
        BookQuickFilter.IN_PROGRESS -> StringRes.books_filter_in_progress
        BookQuickFilter.CACHED -> StringRes.books_filter_cached
        BookQuickFilter.HAS_EBOOK -> StringRes.books_filter_ebook
        BookQuickFilter.HAS_READALOUD -> StringRes.books_filter_readaloud
        BookQuickFilter.IN_SERIES -> StringRes.books_filter_in_series
    }
