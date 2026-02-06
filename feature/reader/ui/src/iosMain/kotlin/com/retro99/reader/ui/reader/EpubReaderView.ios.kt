package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.bridge.EpubReaderSettings
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow

/**
 * iOS implementation of EPUB reader using Readium Swift via bridge.
 *
 * The [publication] object implements [EpubNavigatorController], so we use it
 * directly for navigation instead of creating a separate controller wrapper.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    commands: Flow<ReaderCommand>,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    modifier: Modifier,
) {
    val readerViewController = remember(publication) {
        publication.bridge.createReaderViewController(
            settings = EpubReaderSettings.from(
                settings = publication.initialSettings,
                initialPosition = publication.initialPosition,
            ),
        )
    }

    // Use common command handling logic
    HandleNavigatorCommands(
        navigator = publication,
        commands = commands,
    )

    // Use common observation logic for location changes
    ObserveLocationChanges(
        navigator = publication,
        initialPosition = publication.initialPosition,
        intentDispatcher = intentDispatcher,
    )

    // Use common observation logic for audio playback state
    ObserveAudioPlaybackState(
        navigator = publication,
        intentDispatcher = intentDispatcher,
    )

    val currentViewController = readerViewController
    if (currentViewController == null) {
        // Publication not yet loaded - the LaunchedEffect will handle it
        return
    }

    DisposableEffect(bookUuid) {
        onDispose {
            // Cleanup when the composable is disposed
            publication.close()
        }
    }

    UIKitViewController(
        factory = { currentViewController },
        modifier = modifier.fillMaxSize(),
        update = {},
        onRelease = {},
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )
}
