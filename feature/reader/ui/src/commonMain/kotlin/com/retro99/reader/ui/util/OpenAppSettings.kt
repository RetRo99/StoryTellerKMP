package com.retro99.reader.ui.util

import androidx.compose.runtime.Composable

/**
 * Opens the app settings screen where the user can enable permissions.
 * This is a platform-specific function.
 */
@Composable
expect fun rememberOpenAppSettings(): () -> Unit

