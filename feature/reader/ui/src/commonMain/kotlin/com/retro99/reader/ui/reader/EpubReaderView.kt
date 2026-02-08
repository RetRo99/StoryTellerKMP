package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.navigator.EpubNavigatorController
import com.retro99.reader.ui.navigator.EpubNavigatorControllerNew
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific EPUB reader view.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 *
 * @param bookUuid The unique identifier of the book
 * @param publication The opened EPUB publication
 * @param commands Flow of commands from ViewModel for navigation and settings
 * @param intentDispatcher Dispatcher for sending intents to the ViewModel
 * @param modifier The modifier to apply to the view
 */
@Composable
internal expect fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    commands: Flow<ReaderCommand>,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    bookController: EpubNavigatorControllerNew,
    modifier: Modifier = Modifier,
)

/**
 * Common composable that handles command execution for a navigator controller.
 * This should be called by platform implementations when the navigator is ready.
 *
 * @param navigator The navigator controller to execute commands on
 * @param commands Flow of commands from ViewModel
 */
@Composable
internal fun HandleNavigatorCommands(
    navigator: EpubNavigatorController?,
    commands: Flow<ReaderCommand>,
) {
    // Collect and execute navigation commands
    LaunchedEffect(navigator) {
        navigator?.let { controller ->
            commands.collect { command ->
                when (command) {
                    is ReaderCommand.GoToNextPage -> controller.goToNextPage()
                    is ReaderCommand.GoToPreviousPage -> controller.goToPreviousPage()
                    is ReaderCommand.GoToChapter -> controller.goToChapter(command.href)
                    is ReaderCommand.ApplySettings -> controller.setSettings(command.settings)
                    is ReaderCommand.GoToPosition -> controller.goToPosition(command.position)
                    is ReaderCommand.StartPlayback -> {
                        controller.playAudio(command.initialPositionMs)
                    }

                    is ReaderCommand.PausePlayback -> {
                        controller.pauseAudio()
                    }

                    is ReaderCommand.ResumePlayback -> {
                        controller.resumeAudio()
                    }

                    is ReaderCommand.SeekToAudioPosition -> {
                        controller.seekToAudioPosition(command.audioTimestampMs)
                    }

                    is ReaderCommand.SetPlaybackSpeed -> {
                        controller.setPlaybackSpeed(command.speed)
                    }

                    is ReaderCommand.SkipForward -> {
                        controller.skipForward()
                    }

                    is ReaderCommand.SkipBackward -> {
                        controller.skipBackward()
                    }
                }
            }
        }
    }
}

/**
 * Common composable that observes location changes from the navigator and dispatches intents.
 * Copies the initial position and updates only the location-related fields,
 * preserving the original UUID and createdAt timestamp.
 *
 * @param navigator The navigator controller to observe
 * @param initialPosition The initial position to copy from
 * @param intentDispatcher Dispatcher for sending intents to the ViewModel
 */
@Composable
internal fun ObserveLocationChanges(
    navigator: EpubNavigatorController?,
    initialPosition: PositionUiModel?,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
) {
    LaunchedEffect(navigator, initialPosition) {
        if (initialPosition == null) return@LaunchedEffect

        navigator?.currentLocator?.collect { locator ->
            val positionUiModel = initialPosition.copy(
                href = locator.href,
                type = locator.type,
                title = locator.title,
                progression = locator.progression,
                position = locator.position,
                totalProgression = locator.totalProgression,
            )
            intentDispatcher(ReaderIntent.UpdatePosition(positionUiModel))
        }
    }
}

/**
 * Common composable that observes audio playback state changes from the navigator.
 * This is used for ReadAloud books to track audio position and playing state.
 *
 * @param navigator The navigator controller to observe
 * @param intentDispatcher Dispatcher for sending intents to the ViewModel
 */
@Composable
internal fun ObserveAudioPlaybackState(
    navigator: EpubNavigatorController?,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
) {
    // Observe when media player becomes ready
    LaunchedEffect(navigator) {
        navigator?.isPlayerReady?.collect { isReady ->
            if (isReady) {
                intentDispatcher(ReaderIntent.MediaPlayerReady)
            }
        }
    }

    // Observe audio position updates
    LaunchedEffect(navigator) {
        navigator?.audioPositionState?.collect { state ->
            intentDispatcher(
                ReaderIntent.UpdateAudioPosition(
                    positionMs = state.currentPositionMs,
                    totalDurationMs = state.totalDurationMs,
                ),
            )
        }
    }

    // Observe playing state changes
    LaunchedEffect(navigator) {
        navigator?.isPlayingState?.collect { isPlaying ->
            intentDispatcher(ReaderIntent.UpdatePlayingState(isPlaying = isPlaying))
        }
    }
}

/**
 * Common composable that observes permission denied dialog state from the navigator.
 * Shows a dialog when notification permission is denied on Android.
 *
 * @param navigator The navigator controller to observe
 * @param showDialog Whether to show the permission denied dialog
 * @param showRationale Whether to show rationale (can ask again) vs settings (permanently denied)
 * @param onOpenSettings Callback to open app settings
 * @param onTryAgain Callback when user wants to try requesting permission again
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
internal fun ObservePermissionDeniedDialog(
    navigator: EpubNavigatorController?,
    showDialog: Boolean,
    showRationale: Boolean = false,
    onOpenSettings: () -> Unit,
    onTryAgain: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        NotificationPermissionDeniedDialog(
            showRationale = showRationale,
            onOpenSettings = {
                onOpenSettings()
                navigator?.dismissPermissionDeniedDialog()
            },
            onTryAgain = {
                onTryAgain()
                navigator?.dismissPermissionDeniedDialog()
            },
            onDismiss = {
                onDismiss()
                navigator?.dismissPermissionDeniedDialog()
            },
        )
    }
}
