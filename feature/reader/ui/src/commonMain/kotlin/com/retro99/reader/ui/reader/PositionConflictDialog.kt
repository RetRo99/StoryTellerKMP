package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.retro99.books.ui.components.PositionConflictDialogContent
import com.retro99.reader.ui.model.PositionConflictUiModel
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import resources.translations.reader_conflict_chapter
import resources.translations.reader_conflict_local_title
import resources.translations.reader_conflict_progress
import resources.translations.reader_conflict_remote_title

/**
 * Dialog for resolving position conflicts with full position details.
 * Used in the reader when we have complete position information.
 */
@Composable
fun PositionConflictDialog(
    conflict: PositionConflictUiModel,
    onUseLocal: () -> Unit,
    onUseRemote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PositionConflictDialogContent(
        localContent = {
            PositionCard(
                title = stringResource(StringRes.reader_conflict_local_title),
                position = conflict.localPosition,
            )
        },
        remoteContent = {
            PositionCard(
                title = stringResource(StringRes.reader_conflict_remote_title),
                position = conflict.remotePosition,
            )
        },
        onUseLocal = onUseLocal,
        onUseRemote = onUseRemote,
        onDismissRequest = { /* Don't allow dismiss without choosing */ },
        modifier = modifier,
    )
}

@Composable
private fun PositionCard(
    title: String,
    position: PositionUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            position.title?.let { chapterTitle ->
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            val chapterIndex = position.chapterIndex
            val totalChapters = position.totalChapters
            if (chapterIndex != null && totalChapters != null) {
                Text(
                    text = stringResource(
                        StringRes.reader_conflict_chapter,
                        chapterIndex + 1,
                        totalChapters,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            position.totalProgression?.let { progress ->
                Text(
                    text = stringResource(
                        StringRes.reader_conflict_progress,
                        (progress * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
