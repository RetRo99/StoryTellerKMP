package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication
import platform.UIKit.setStatusBarHidden

/**
 * iOS implementation of HideSystemBars.
 * Uses UIApplication to hide the status bar for an immersive reading experience.
 */
@Composable
actual fun HideSystemBars() {
    DisposableEffect(Unit) {
        // Hide status bar
        UIApplication.sharedApplication.setStatusBarHidden(true, animated = true)

        onDispose {
            // Restore status bar
            UIApplication.sharedApplication.setStatusBarHidden(false, animated = true)
        }
    }
}

@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    // No-op on iOS for now.
}

