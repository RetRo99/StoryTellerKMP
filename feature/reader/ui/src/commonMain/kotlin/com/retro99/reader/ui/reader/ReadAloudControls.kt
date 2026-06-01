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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.IntentDispatcher

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
    sleepTimerRemainingMs: Long?,
    showAudioProgressBar: Boolean?,
    areControlsVisible: Boolean,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    onInteraction: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    onControlsDialogVisibilityChanged: (Boolean) -> Unit = {},
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
        // Determine if SeekBar should be visible based on showAudioProgressBar setting
        // null = show when controls are visible (on tap, default), false = never show
        val isSeekBarVisible = showAudioProgressBar != false && areControlsVisible

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (isSeekBarVisible) {
                SeekBar(
                    currentPositionMs = currentPositionMs,
                    totalDurationMs = totalDurationMs,
                    onInteraction = onInteraction,
                ) { interactingDispatcher(ReaderIntent.SeekTo(it)) }
                Spacer(modifier = Modifier.height(8.dp))
            }
            PlaybackControlsRow(
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                currentPositionMs = currentPositionMs,
                totalDurationMs = totalDurationMs,
                sleepTimerRemainingMs = sleepTimerRemainingMs,
                intentDispatcher = interactingDispatcher,
                onControlsDialogVisibilityChanged = onControlsDialogVisibilityChanged,
            )
        }
    }
}

@Composable
private fun PlaybackControlsRow(
    isPlaying: Boolean,
    playbackSpeed: Float,
    currentPositionMs: Long,
    totalDurationMs: Long?,
    sleepTimerRemainingMs: Long?,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    onControlsDialogVisibilityChanged: (Boolean) -> Unit,
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
        SleepTimerButton(
            currentPositionMs = currentPositionMs,
            totalDurationMs = totalDurationMs,
            sleepTimerRemainingMs = sleepTimerRemainingMs,
            intentDispatcher = intentDispatcher,
            onCustomTimerDialogVisibilityChanged = onControlsDialogVisibilityChanged,
        )
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

@Composable
private fun SleepTimerButton(
    currentPositionMs: Long,
    totalDurationMs: Long?,
    sleepTimerRemainingMs: Long?,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    onCustomTimerDialogVisibilityChanged: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomTimerDialog by remember { mutableStateOf(false) }
    val remainingToEndMs = totalDurationMs
        ?.minus(currentPositionMs)
        ?.takeIf { it > 0L }

    LaunchedEffect(showCustomTimerDialog) {
        onCustomTimerDialogVisibilityChanged(showCustomTimerDialog)
    }

    DisposableEffect(Unit) {
        onDispose { onCustomTimerDialogVisibilityChanged(false) }
    }

    TextButton(onClick = { expanded = true }) {
        Icon(
            imageVector = if (sleepTimerRemainingMs == null) Icons.Default.Timer else Icons.Default.TimerOff,
            contentDescription = "Sleep timer",
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = sleepTimerRemainingMs?.let { formatSleepTimerLabel(it) } ?: "Timer",
            style = MaterialTheme.typography.labelLarge,
        )
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            SleepTimerPreset.entries.forEach { preset ->
                DropdownMenuItem(
                    text = { Text(preset.label) },
                    onClick = {
                        intentDispatcher(ReaderIntent.StartSleepTimer(preset.durationMs))
                        expanded = false
                    },
                )
            }
            DropdownMenuItem(
                text = { Text("Custom...") },
                onClick = {
                    expanded = false
                    showCustomTimerDialog = true
                },
            )
            if (remainingToEndMs != null) {
                DropdownMenuItem(
                    text = { Text("End of audio") },
                    onClick = {
                        intentDispatcher(ReaderIntent.StartSleepTimer(remainingToEndMs))
                        expanded = false
                    },
                )
            }
            if (sleepTimerRemainingMs != null) {
                DropdownMenuItem(
                    text = { Text("Cancel timer") },
                    onClick = {
                        intentDispatcher(ReaderIntent.CancelSleepTimer)
                        expanded = false
                    },
                )
            }
        }
    }

    if (showCustomTimerDialog) {
        SleepTimerDurationDialog(
            title = "Custom sleep timer",
            message = "Choose how long playback should continue before pausing.",
            confirmLabel = "Start",
            dismissLabel = "Cancel",
            initialMinutes = sleepTimerRemainingMs
                ?.let { ((it + 59_999L) / 60_000L).toInt().coerceAtLeast(1) }
                ?: 5,
            onConfirm = { minutes ->
                intentDispatcher(ReaderIntent.StartSleepTimer(minutes * 60_000L))
                showCustomTimerDialog = false
            },
            onDismiss = { showCustomTimerDialog = false },
        )
    }
}

private enum class SleepTimerPreset(
    val label: String,
    val durationMs: Long,
) {
    FiveMinutes("5 minutes", 5 * 60_000L),
    TenMinutes("10 minutes", 10 * 60_000L),
    FifteenMinutes("15 minutes", 15 * 60_000L),
    ThirtyMinutes("30 minutes", 30 * 60_000L),
    SixtyMinutes("60 minutes", 60 * 60_000L),
}

