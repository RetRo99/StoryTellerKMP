package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable

/**
 * Composable effect that hides the system bars (status bar, navigation bar)
 * when the composable is in the composition, and restores them when disposed.
 *
 * This is used in the reader screen to provide an immersive reading experience.
 */
@Composable
expect fun HideSystemBars()

