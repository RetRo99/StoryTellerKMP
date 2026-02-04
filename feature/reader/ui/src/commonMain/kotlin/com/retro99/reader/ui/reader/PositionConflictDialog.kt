package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.retro99.reader.ui.model.PositionConflictUiModel
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import resources.translations.reader_conflict_chapter
import resources.translations.reader_conflict_local_title
import resources.translations.reader_conflict_message
import resources.translations.reader_conflict_progress
import resources.translations.reader_conflict_remote_title
import resources.translations.reader_conflict_title
import resources.translations.reader_conflict_use_local
import resources.translations.reader_conflict_use_remote

@Composable
fun PositionConflictDialog(
    conflict: PositionConflictUiModel,
    onUseLocal: () -> Unit,
    onUseRemote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { /* Don't allow dismiss without choosing */ },
        modifier = modifier,
        title = {
            Text(
                text = stringResource(StringRes.reader_conflict_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(StringRes.reader_conflict_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PositionCard(
                        title = stringResource(StringRes.reader_conflict_local_title),
                        position = conflict.localPosition,
                        modifier = Modifier.weight(1f),
                    )
                    PositionCard(
                        title = stringResource(StringRes.reader_conflict_remote_title),
                        position = conflict.remotePosition,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onUseLocal) {
                    Text(stringResource(StringRes.reader_conflict_use_local))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onUseRemote) {
                    Text(stringResource(StringRes.reader_conflict_use_remote))
                }
            }
        },
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

