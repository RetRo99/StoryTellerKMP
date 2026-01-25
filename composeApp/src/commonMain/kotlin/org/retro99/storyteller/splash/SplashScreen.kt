package org.retro99.storyteller.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Splash screen shown on app startup.
 * Displays a loading indicator while the app initializes and auth state is checked.
 *
 * @param modifier Optional modifier for the root layout
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // You can replace this with a logo or animation
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

