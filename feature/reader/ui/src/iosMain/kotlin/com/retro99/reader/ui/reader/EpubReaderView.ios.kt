package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.controller.EpubReaderController
import com.retro99.reader.ui.controller.IosEpubReaderController
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.koinInject

/**
 * iOS implementation of EPUB reader using Readium Swift via bridge.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    initialSettings: ReaderSettingsDomainModel,
    modifier: Modifier,
) {
    val controller: EpubReaderController = koinInject()
    val iosController = controller as IosEpubReaderController

    val readerViewController = remember(bookUuid) {
        iosController.createReaderViewController(initialSettings.fontSize)
    } ?: run {
        ReaderErrorView(message = "EPUB publication not ready", modifier = modifier)
        return
    }

    DisposableEffect(bookUuid) {
        onDispose {
            // Cleanup when the composable is disposed
            iosController.closePublication()
        }
    }

    UIKitViewController(
        factory = { readerViewController },
        modifier = modifier.fillMaxSize(),
        update = {},
        onRelease = {},
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )
}
