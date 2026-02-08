package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import co.touchlab.kermit.Logger
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.bridge.EpubReaderSettings
import com.retro99.reader.ui.navigator.EpubNavigatorControllerNew
import com.retro99.reader.ui.publication.EpubPublication
import com.retro99.reader.ui.util.rememberOpenAppSettings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import platform.UIKit.UIViewController

private val logger = Logger.withTag("EpubReaderView.iOS")

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
    bookController: EpubNavigatorControllerNew,
    modifier: Modifier,
) {
    logger.d { "EpubReaderView composing for bookUuid=$bookUuid" }

    // Key the state by bookUuid to ensure it persists across recompositions
    // but resets when opening a different book
    var readerViewController by remember(bookUuid) { mutableStateOf<UIViewController?>(null) }

    // Create the reader view controller when the publication is ready
    LaunchedEffect(publication) {
        logger.d { "LaunchedEffect started for publication" }
        logger.d { "Publication bridge: ${publication.bridge}" }
        logger.d { "Initial settings: ${publication.initialSettings}" }
        logger.d { "Initial position: ${publication.initialPosition}" }

        // Try to create the view controller, retrying if needed
        var attempts = 0
        while (readerViewController == null && attempts < 10) {
            logger.d { "Attempt ${attempts + 1} to create reader view controller" }
            val settings = EpubReaderSettings.from(
                settings = publication.initialSettings,
                initialPosition = publication.initialPosition,
            )
            logger.d { "Created EpubReaderSettings: $settings" }

            val viewController = publication.bridge.createReaderViewController(settings = settings)
            logger.d { "createReaderViewController returned: $viewController" }

            if (viewController != null) {
                logger.i { "Successfully created reader view controller on attempt ${attempts + 1}" }
                readerViewController = viewController
                // Initialize media overlays now that the navigator exists
                // This is needed because the Swift MediaOverlayPlayer needs access to
                // the navigator's current location for proper initialization
                logger.d { "Initializing media overlays after navigator creation" }
                publication.initializeMediaOverlaysIfNeeded()
            } else {
                attempts++
                logger.w { "createReaderViewController returned null, attempt $attempts/10" }
                delay(100) // Wait a bit before retrying
            }
        }

        if (readerViewController == null) {
            logger.e { "Failed to create reader view controller after 10 attempts" }
        }
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

    // Observe permission denied dialog state (iOS doesn't need this, but included for consistency)
    val showPermissionDeniedDialog by publication.showPermissionDeniedDialog
        .collectAsState(initial = false)
    val showPermissionRationale by publication.showPermissionRationale
        .collectAsState(initial = false)
    val openAppSettings = rememberOpenAppSettings()

    ObservePermissionDeniedDialog(
        navigator = publication,
        showDialog = showPermissionDeniedDialog,
        showRationale = showPermissionRationale,
        onOpenSettings = openAppSettings,
        onTryAgain = {
            // User wants to try again - trigger play which will re-request permission
            publication.playAudio()
        },
        onDismiss = { /* Dialog dismissed without opening settings */ },
    )

    DisposableEffect(bookUuid) {
        logger.d { "DisposableEffect started for bookUuid=$bookUuid" }
        onDispose {
            logger.d { "DisposableEffect onDispose - closing publication" }
            // Clear the local view controller reference first
            readerViewController = null
            // Then close the publication which will clean up the Swift side
            publication.close()
        }
    }

    val currentViewController = readerViewController
    if (currentViewController != null) {
        // Use key to force recreation when bookUuid changes
        // This ensures a fresh UIKitViewController is created for each book
        key(bookUuid) {
            UIKitViewController(
                factory = { currentViewController },
                modifier = modifier.fillMaxSize(),
                update = { viewController ->
                    // Force the view to layout within its parent bounds
                    viewController.view.setNeedsLayout()
                    viewController.view.layoutIfNeeded()
                },
                properties = UIKitInteropProperties(
                    isInteractive = true,
                    isNativeAccessibilityEnabled = true,
                ),
            )
        }
    } else {
        // Show loading indicator while waiting for the view controller
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
