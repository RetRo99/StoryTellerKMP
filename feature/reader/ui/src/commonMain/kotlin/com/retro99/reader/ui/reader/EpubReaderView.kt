package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific EPUB reader view.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this is a placeholder until Readium iOS integration is added.
 */
@Composable
internal expect fun EpubReaderView(
    bookUuid: String,
    modifier: Modifier = Modifier,
)

