package com.retro99.parrot

import androidx.compose.ui.window.ComposeUIViewController
import com.retro99.base.AppInitializer
import com.retro99.parrot.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        val koinApp = initKoin()
        koinApp.koin.getAll<AppInitializer>().forEach { it.initialize() }
    },
) { App() }

