package org.retro99.storyteller

import androidx.compose.ui.window.ComposeUIViewController
import org.retro99.storyteller.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }