package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.navigator.IosEpubNavigatorController
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.UIKit.UIViewController

private val logger = Logger.withTag("EpubReaderView.iOS")

/**
 * iOS implementation of EPUB reader using Readium Swift via bridge.
 *
 * Note: Audio playback is now managed by the ViewModel via AudioController,
 * which is created as a Koin Factory with EpubPublication as a parameter.
 * The AudioController initializes media overlays in its init block.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    bookController: BookController,
    modifier: Modifier,
) {
    logger.d { "EpubReaderView composing for bookUuid=$bookUuid" }

    val navigator = bookController as? IosEpubNavigatorController

    // Key the state by bookUuid to ensure it persists across recompositions
    // but resets when opening a different book
    var readerViewController by remember(bookUuid) { mutableStateOf<UIViewController?>(null) }

    // Create the reader view controller when the publication is ready
    LaunchedEffect(publication, navigator) {
        logger.d { "LaunchedEffect started for publication" }
        logger.d { "Initial settings: ${publication.initialSettings}" }

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
                // Note: Media overlays are now initialized by IosAudioController in its init block
                // when it's created by the ViewModel after the publication is opened
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

    // Note: Audio playback state observation and permission dialogs are now handled
    // by the ViewModel via the AudioController. iOS doesn't require notification
    // permission for audio playback, so permission dialogs are not needed here.

    DisposableEffect(bookUuid) {
        logger.d { "DisposableEffect started for bookUuid=$bookUuid" }
        onDispose {
            logger.d { "DisposableEffect onDispose - closing publication" }
            readerViewController = null
            navigator?.close()
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
