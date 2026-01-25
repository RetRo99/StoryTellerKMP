package org.retro99.storyteller

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.retro99.storyteller.navigation.RootNavigation

@Composable
fun App() {
    MaterialTheme {
        RootNavigation()
    }
}