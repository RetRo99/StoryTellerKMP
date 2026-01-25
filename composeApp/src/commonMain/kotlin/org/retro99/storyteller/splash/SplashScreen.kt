package org.retro99.storyteller.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * Splash screen shown on app startup.
 * Checks authentication state and navigates accordingly.
 *
 * @param onSplashComplete Callback with isLoggedIn parameter.
 *                         true = navigate to Home, false = navigate to Login
 */
@Composable
fun SplashScreen(
    onSplashComplete: (isLoggedIn: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: Replace with actual auth state check
    LaunchedEffect(Unit) {
        delay(1500) // Simulate loading
        // For now, always navigate to login (not logged in)
        onSplashComplete(false)
    }
    
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

