package com.retro99.reader.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOpenAppSettings(): () -> Unit = remember {
    { }
}
