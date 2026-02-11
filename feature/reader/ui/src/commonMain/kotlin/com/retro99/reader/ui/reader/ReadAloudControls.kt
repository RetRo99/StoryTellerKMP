package com.retro99.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.model.ReadAloudHighlightColor

/**
 * Media controls overlay for ReadAloud books.
 *
 * @param isPlaying Whether audio is currently playing
 * @param currentPositionMs Current playback position in milliseconds
 * @param totalDurationMs Total duration in milliseconds
 * @param playbackSpeed Current playback speed multiplier
 * @param intentDispatcher Dispatcher for reader intents
 * @param onInteraction Callback invoked when user interacts with controls (resets auto-hide timer)
 * @param modifier Modifier for the controls container
 */
/** Threshold in dp for how far user needs to drag to dismiss */
private const val DISMISS_THRESHOLD_DP = 80

@Composable
internal fun ReadAloudControls(
    isPlaying: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long?,
    playbackSpeed: Float,
    highlightColor: ReadAloudHighlightColor,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    onInteraction: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Wrapper that calls onInteraction before dispatching intent
    val interactingDispatcher = IntentDispatcher<ReaderIntent> { intent ->
        onInteraction()
        intentDispatcher(intent)
    }

    // Track vertical offset for drag gesture
    var offsetY by remember { mutableFloatStateOf(0f) }
    val dismissThresholdPx = with(LocalDensity.current) { DISMISS_THRESHOLD_DP.dp.toPx() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, offsetY.toInt().coerceAtLeast(0)) }
            .draggable(
                state = rememberDraggableState { delta ->
                    // Only allow dragging down (positive delta)
                    offsetY = (offsetY + delta).coerceAtLeast(0f)
                },
                orientation = Orientation.Vertical,
                onDragStarted = { onInteraction() },
                onDragStopped = { velocity ->
                    // Dismiss if dragged past threshold or flung with enough velocity
                    if (offsetY > dismissThresholdPx || velocity > 500f) {
                        onSwipeDown()
                        // Don't reset offset - let AnimatedVisibility handle exit from current position
                    } else {
                        // Only reset if not dismissing (snap back)
                        offsetY = 0f
                    }
                },
            ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            SeekBar(
                currentPositionMs = currentPositionMs,
                totalDurationMs = totalDurationMs,
                onInteraction = onInteraction,
            ) { interactingDispatcher(ReaderIntent.SeekTo(it)) }
            Spacer(modifier = Modifier.height(8.dp))
            PlaybackControlsRow(isPlaying, playbackSpeed, highlightColor, interactingDispatcher)
        }
    }
}

@Composable
private fun PlaybackControlsRow(
    isPlaying: Boolean,
    playbackSpeed: Float,
    highlightColor: ReadAloudHighlightColor,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaybackSpeedButton(playbackSpeed) { intentDispatcher(ReaderIntent.SetPlaybackSpeed(it)) }
        IconButton(onClick = { intentDispatcher(ReaderIntent.SkipBackward()) }) {
            Icon(Icons.Default.Replay10, "Skip backward", Modifier.size(32.dp))
        }
        IconButton(
            onClick = { intentDispatcher(ReaderIntent.TogglePlayback) },
            modifier = Modifier.size(56.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                if (isPlaying) "Pause" else "Play",
                Modifier.size(32.dp),
                MaterialTheme.colorScheme.onPrimary,
            )
        }
        IconButton(onClick = { intentDispatcher(ReaderIntent.SkipForward()) }) {
            Icon(Icons.Default.Forward10, "Skip forward", Modifier.size(32.dp))
        }
        HighlightColorButton(highlightColor) {
            intentDispatcher(ReaderIntent.SetHighlightColor(it))
        }
    }
}

@Composable
private fun SeekBar(
    currentPositionMs: Long,
    totalDurationMs: Long?,
    onInteraction: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    val duration = totalDurationMs ?: 0L
    val progress = if (duration > 0) currentPositionMs.toFloat() / duration else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = {
                onInteraction()
                if (duration > 0) onSeek((it * duration).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(formatDuration(currentPositionMs), style = MaterialTheme.typography.bodySmall)
            Text(formatDuration(duration), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PlaybackSpeedButton(currentSpeed: Float, onSpeedSelected: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    TextButton(onClick = { expanded = true }) {
        Text("${currentSpeed}x", style = MaterialTheme.typography.labelLarge)
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x") },
                    onClick = { onSpeedSelected(speed); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun HighlightColorButton(
    currentColor: ReadAloudHighlightColor,
    onColorSelected: (ReadAloudHighlightColor) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.FormatColorFill,
            contentDescription = "Highlight color",
            modifier = Modifier.size(24.dp),
            tint = Color(currentColor.argb),
        )
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            ReadAloudHighlightColor.entries.forEach { color ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(color.argb), CircleShape),
                            )
                            Text(color.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    },
                    onClick = { onColorSelected(color); expanded = false },
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val paddedMinutes = minutes.toString().padStart(2, '0')
    val paddedSeconds = seconds.toString().padStart(2, '0')
    return if (hours > 0) {
        "$hours:$paddedMinutes:$paddedSeconds"
    } else {
        "$minutes:$paddedSeconds"
    }
}

