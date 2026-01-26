package org.retro99.storyteller

import androidx.compose.ui.window.ComposeUIViewController
import com.retro99.base.buildconfig.di.platformBuildConfigModule
import org.retro99.storyteller.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin(additionalModules = listOf(platformBuildConfigModule))
    }
) { App() }