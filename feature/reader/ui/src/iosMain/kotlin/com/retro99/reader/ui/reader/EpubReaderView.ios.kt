package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.controller.EpubReaderController
import com.retro99.reader.ui.controller.IosEpubReaderController
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

/**
 * iOS implementation of EPUB reader using Readium Swift via bridge.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    localFilePath: String,
    settings: ReaderSettingsDomainModel,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier,
) {
    val controller: EpubReaderController = koinInject()
    val iosController = controller as IosEpubReaderController

    var readerViewController by remember { mutableStateOf<UIViewController?>(null) }
    var isPublicationReady by remember { mutableStateOf(false) }

    // Open publication when localFilePath is available
    LaunchedEffect(localFilePath) {
        if (localFilePath.isNotEmpty()) {
            val success = iosController.openPublication(localFilePath)
            if (success) {
                val viewController = iosController.createReaderViewController(settings)
                if (viewController != null) {
                    readerViewController = viewController
                    isPublicationReady = true
                }
            }
        }
    }

    // Apply settings when they change and publication is ready
    LaunchedEffect(settings, isPublicationReady) {
        if (isPublicationReady) {
            iosController.setSettings(settings)
        }
    }

    // Show loading state if view controller is not ready
    val currentViewController = readerViewController
    if (currentViewController == null) {
        // Publication not yet loaded - the LaunchedEffect will handle it
        return
    }

    DisposableEffect(bookUuid) {
        onDispose {
            // Cleanup when the composable is disposed
            iosController.closePublication()
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
