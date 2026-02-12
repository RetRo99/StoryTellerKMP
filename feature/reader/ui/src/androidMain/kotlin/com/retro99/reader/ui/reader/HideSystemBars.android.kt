package com.retro99.reader.ui.reader

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Android implementation of HideSystemBars.
 * Uses WindowInsetsController to hide the status bar and navigation bar,
 * providing an immersive fullscreen experience for the reader.
 */
@Composable
actual fun HideSystemBars() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Store original system UI visibility state
        val originalSystemBarsBehavior = insetsController.systemBarsBehavior

        // Hide system bars
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Keep screen on while reading
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            // Restore system bars
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                systemBarsBehavior = originalSystemBarsBehavior
            }

            // Remove keep screen on flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

