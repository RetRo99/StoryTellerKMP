package com.retro99.books.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import resources.translations.reader_conflict_local_title
import resources.translations.reader_conflict_message
import resources.translations.reader_conflict_progress
import resources.translations.reader_conflict_remote_title
import resources.translations.reader_conflict_title
import resources.translations.reader_conflict_use_local
import resources.translations.reader_conflict_use_remote

/**
 * Dialog for resolving position conflicts with just progress percentages.
 * Used in book details when we only have progress info.
 */
@Composable
fun PositionConflictDialog(
    localProgressPercent: Int,
    remoteProgressPercent: Int,
    onUseLocal: () -> Unit,
    onUseRemote: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PositionConflictDialogContent(
        localContent = {
            ProgressCard(
                title = stringResource(StringRes.reader_conflict_local_title),
                progressPercent = localProgressPercent,
            )
        },
        remoteContent = {
            ProgressCard(
                title = stringResource(StringRes.reader_conflict_remote_title),
                progressPercent = remoteProgressPercent,
            )
        },
        onUseLocal = onUseLocal,
        onUseRemote = onUseRemote,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    )
}

/**
 * Shared dialog content for position conflict resolution.
 * Can be used with different card content (simple progress or full position details).
 */
@Composable
fun PositionConflictDialogContent(
    localContent: @Composable () -> Unit,
    remoteContent: @Composable () -> Unit,
    onUseLocal: () -> Unit,
    onUseRemote: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                    Box(modifier = Modifier.weight(1f)) { localContent() }
                    Box(modifier = Modifier.weight(1f)) { remoteContent() }
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

/**
 * Simple card showing just progress percentage.
 * Used when we don't have full position details.
 */
@Composable
private fun ProgressCard(
    title: String,
    progressPercent: Int,
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
            Text(
                text = stringResource(
                    StringRes.reader_conflict_progress,
                    progressPercent,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

