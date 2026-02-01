package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.bridge.EpubReaderSettings
import com.retro99.reader.ui.navigator.EpubNavigatorController
import com.retro99.reader.ui.navigator.IosEpubNavigatorController
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow

/**
 * iOS implementation of EPUB reader using Readium Swift via bridge.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    settings: ReaderSettingsDomainModel,
    commands: Flow<ReaderCommand>,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier,
) {
    val bridge = publication.bridge

    val readerViewController = remember(publication) {
        bridge.createReaderViewController(settings = EpubReaderSettings.from(settings))
    }
    val navigatorController: EpubNavigatorController = remember(publication) {
        IosEpubNavigatorController(bridge)
    }

    LaunchedEffect(navigatorController) {
        commands.collect { command ->
            when (command) {
                is ReaderCommand.GoToNextPage -> navigatorController.goToNextPage()
                is ReaderCommand.GoToPreviousPage -> navigatorController.goToPreviousPage()
                is ReaderCommand.GoToChapter -> navigatorController.goToChapter(command.href)
                is ReaderCommand.ApplySettings -> navigatorController.setSettings(command.settings)
            }
        }
    }

    LaunchedEffect(settings, navigatorController) {
        navigatorController.setSettings(settings)
    }

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
