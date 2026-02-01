package org.retro99.storyteller

import androidx.compose.ui.window.ComposeUIViewController
import com.retro99.base.AppInitializer
import org.retro99.storyteller.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        val koinApp = initKoin()
        koinApp.koin.getAll<AppInitializer>().forEach { it.initialize() }
    },
) { App() }