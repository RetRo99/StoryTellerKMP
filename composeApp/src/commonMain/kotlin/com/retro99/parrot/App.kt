package com.retro99.parrot

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.retro99.parrot.navigation.RootNavigation

// E-ink optimized color scheme
// Best practices for e-ink:
// - High contrast (pure black on white)
// - Limited grayscale palette (e-ink typically supports 16 levels)
// - Avoid gradients and animations
// - Use light gray (#F0F0F0) for subtle surface differentiation
private val EinkLightGray = Color(0xFFF0F0F0)
private val EinkDarkGray = Color(0xFF333333)

private val EinkColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = EinkLightGray,
    onPrimaryContainer = Color.Black,
    secondary = EinkDarkGray,
    onSecondary = Color.White,
    secondaryContainer = EinkLightGray,
    onSecondaryContainer = Color.Black,
    tertiary = EinkDarkGray,
    onTertiary = Color.White,
    tertiaryContainer = EinkLightGray,
    onTertiaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = EinkLightGray,
    onSurfaceVariant = EinkDarkGray,
    outline = EinkDarkGray,
    outlineVariant = EinkLightGray,
    error = Color.Black,
    onError = Color.White,
    errorContainer = EinkLightGray,
    onErrorContainer = Color.Black,
)

@Composable
fun App() {
    val platform = getPlatform()
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        platform.isEink -> EinkColorScheme
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            RootNavigation(
                modifier = Modifier
                    .navigationBarsPadding(),
            )
        }
    }
}

