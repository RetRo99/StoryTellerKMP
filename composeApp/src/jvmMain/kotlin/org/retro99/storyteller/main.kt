package org.retro99.storyteller

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.retro99.storyteller.di.initKoin

fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "StoryTellerKMP",
        ) {
            App()
        }
    }
}