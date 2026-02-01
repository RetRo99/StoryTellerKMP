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
import com.retro99.reader.ui.navigator.EpubNavigatorController
import com.retro99.reader.ui.navigator.IosEpubNavigatorController
import com.retro99.reader.ui.service.EpubPublicationService
import com.retro99.reader.ui.service.IosEpubPublicationService
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
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
    commands: Flow<ReaderCommand>,
    publicationService: EpubPublicationService,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier,
) {
    val iosService = publicationService as IosEpubPublicationService

    var readerViewController by remember { mutableStateOf<UIViewController?>(null) }
    var isPublicationReady by remember { mutableStateOf(false) }
    var navigatorController by remember { mutableStateOf<EpubNavigatorController?>(null) }

    // Open publication when localFilePath is available
    LaunchedEffect(localFilePath) {
        if (localFilePath.isNotEmpty()) {
            val success = iosService.openPublication(localFilePath)
            if (success) {
                val viewController = iosService.createReaderViewController(settings)
                if (viewController != null) {
                    readerViewController = viewController
                    isPublicationReady = true
                    // Create navigator controller from the bridge
                    iosService.bridge?.let { bridge ->
                        navigatorController = IosEpubNavigatorController(bridge)
                    }
                }
            }
        }
    }

    // Collect commands and execute on navigator controller
    LaunchedEffect(navigatorController) {
        navigatorController?.let { controller ->
            commands.collect { command ->
                when (command) {
                    is ReaderCommand.GoToNextPage -> controller.goToNextPage()
                    is ReaderCommand.GoToPreviousPage -> controller.goToPreviousPage()
                    is ReaderCommand.GoToChapter -> controller.goToChapter(command.href)
                    is ReaderCommand.ApplySettings -> controller.setSettings(command.settings)
                }
            }
        }
    }

    // Apply settings when they change and navigator is ready
    LaunchedEffect(settings, navigatorController) {
        navigatorController?.setSettings(settings)
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
            navigatorController = null
            iosService.closePublication()
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
