package com.retro99.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.retro99.reader.ui.model.TocItemUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import resources.translations.reader_toc_title

private const val INDENT_PER_LEVEL_DP = 16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOfContentsSheet(
    tableOfContents: List<TocItemUiModel>,
    currentChapterHref: String?,
    onChapterClick: (href: String) -> Unit,
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
            currentChapterHref = currentChapterHref,
            onChapterClick = onChapterClick,
        )
    }
}

@Composable
private fun TableOfContentsContent(
    tableOfContents: List<TocItemUiModel>,
    currentChapterHref: String?,
    onChapterClick: (href: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Find the index of the current chapter and scroll to it
    val currentChapterIndex = tableOfContents.indexOfFirst { tocItem ->
        currentChapterHref != null && tocItem.href == currentChapterHref
    }

    LaunchedEffect(currentChapterIndex) {
        if (currentChapterIndex >= 0) {
            listState.animateScrollToItem(currentChapterIndex)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.reader_toc_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        HorizontalDivider()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(
                items = tableOfContents,
                key = { index, item -> "${index}_${item.href}" },
            ) { _, tocItem ->
                val isCurrentChapter =
                    currentChapterHref != null && tocItem.href == currentChapterHref
                TocItemRow(
                    tocItem = tocItem,
                    isCurrentChapter = isCurrentChapter,
                    onClick = { onChapterClick(tocItem.href) },
                )
            }
        }
    }
}

@Composable
private fun TocItemRow(
    tocItem: TocItemUiModel,
    isCurrentChapter: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startPadding = (16 + tocItem.level * INDENT_PER_LEVEL_DP).dp

    val backgroundColor = if (isCurrentChapter) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isCurrentChapter -> MaterialTheme.colorScheme.onPrimaryContainer
        tocItem.level == 0 -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = tocItem.title,
        style = if (tocItem.level == 0) {
            MaterialTheme.typography.bodyLarge
        } else {
            MaterialTheme.typography.bodyMedium
        },
        fontWeight = if (isCurrentChapter) FontWeight.Bold else FontWeight.Normal,
        color = textColor,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                start = startPadding,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
    )
}

