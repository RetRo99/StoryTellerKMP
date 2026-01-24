package org.retro99.storyteller

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "StoryTellerKMP",
    ) {
        App()
    }
}