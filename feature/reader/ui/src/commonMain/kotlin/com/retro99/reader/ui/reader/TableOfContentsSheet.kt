package com.retro99.reader.ui.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.retro99.reader.ui.model.TocItemUiModel

private const val INDENT_PER_LEVEL_DP = 16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOfContentsSheet(
    tableOfContents: List<TocItemUiModel>,
    onTocItemClick: (TocItemUiModel) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        TableOfContentsContent(
            tableOfContents = tableOfContents,
            onTocItemClick = onTocItemClick,
        )
    }
}

@Composable
private fun TableOfContentsContent(
    tableOfContents: List<TocItemUiModel>,
    onTocItemClick: (TocItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Table of Contents",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(
                items = tableOfContents,
                key = { it.href },
            ) { tocItem ->
                TocItemRow(
                    tocItem = tocItem,
                    onClick = { onTocItemClick(tocItem) },
                )
            }
        }
    }
}

@Composable
private fun TocItemRow(
    tocItem: TocItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startPadding = (16 + tocItem.level * INDENT_PER_LEVEL_DP).dp

    Text(
        text = tocItem.title,
        style = if (tocItem.level == 0) {
            MaterialTheme.typography.bodyLarge
        } else {
            MaterialTheme.typography.bodyMedium
        },
        color = if (tocItem.level == 0) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = startPadding,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
    )
}

