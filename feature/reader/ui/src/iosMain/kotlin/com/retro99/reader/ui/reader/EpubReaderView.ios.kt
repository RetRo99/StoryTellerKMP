package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of EPUB reader using Readium Swift via bridge.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    modifier: Modifier,
) {
    val bridge = remember { EpubReaderBridgeRegistry.getBridge() }

    if (bridge == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("EPUB Reader bridge not available")
        }
        return
    }

    val readerViewController = remember { bridge.createReaderViewController() }

    if (readerViewController == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("EPUB publication not ready")
        }
        return
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

