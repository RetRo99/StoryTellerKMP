package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.di.koinReaderScopeInject
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.navigator.EpubNavigatorController
import com.retro99.reader.ui.publication.EpubPublication

/**
 * Platform-specific EPUB reader view.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 *
 * @param bookUuid The unique identifier of the book
 * @param publication The opened EPUB publication
 * @param intentDispatcher Dispatcher for sending intents to the ViewModel
 * @param modifier The modifier to apply to the view
 * @param bookController Controller for navigating and controlling the book reader.
 *                       Defaults to injecting from the ReaderScope.
 */
@Composable
internal fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    modifier: Modifier = Modifier,
    bookController: BookController = koinReaderScopeInject(bookUuid),
) {
    EpubReaderViewInternal(
        bookUuid = bookUuid,
        publication = publication,
        intentDispatcher = intentDispatcher,
        bookController = bookController,
        modifier = modifier,
    )
}

/**
 * Internal platform-specific EPUB reader view implementation.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 */
@Composable
internal expect fun EpubReaderViewInternal(
    bookUuid: String,
    publication: EpubPublication,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    bookController: BookController,
    modifier: Modifier = Modifier,
)

/**
 * Common composable that observes double-tap events on sentence elements.
 * When a double-tap is detected, dispatches a PlayFromFragment intent to start
 * audio playback from that sentence.
 *
 * @param bookController The book controller to observe for double-tap events
 * @param intentDispatcher Dispatcher for sending intents to the ViewModel
 */
@Composable
internal fun ObserveSentenceDoubleTapEvents(
    bookController: BookController?,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
) {
    LaunchedEffect(bookController) {
        bookController?.sentenceDoubleTapEvents?.collect { event ->
            intentDispatcher(
                ReaderIntent.PlayFromFragment(
                    fragmentId = event.fragmentId,
                    chapterHref = event.chapterHref,
                )
            )
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
//        navigator?.isPlayerReady?.collect { isReady ->
//            if (isReady) {
//                intentDispatcher(ReaderIntent.MediaPlayerReady)
//            }
//        }
    }

    // Observe audio position updates
    LaunchedEffect(navigator) {
//        navigator?.audioPositionState?.collect { state ->
//            intentDispatcher(
//                ReaderIntent.UpdateAudioPosition(
//                    positionMs = state.currentPositionMs,
//                    totalDurationMs = state.totalDurationMs,
//                ),
//            )
//        }
    }

    // Observe playing state changes
    LaunchedEffect(navigator) {
//        navigator?.isPlayingState?.collect { isPlaying ->
//            intentDispatcher(ReaderIntent.UpdatePlayingState(isPlaying = isPlaying))
//        }
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
//                navigator?.dismissPermissionDeniedDialog()
            },
            onTryAgain = {
                onTryAgain()
//                navigator?.dismissPermissionDeniedDialog()
            },
            onDismiss = {
                onDismiss()
//                navigator?.dismissPermissionDeniedDialog()
            },
        )
    }
}
