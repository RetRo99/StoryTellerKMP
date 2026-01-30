package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * iOS placeholder implementation for EPUB reader.
 * Readium iOS integration is not yet implemented.
 */
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("EPUB Reader not yet available on iOS")
    }
}

