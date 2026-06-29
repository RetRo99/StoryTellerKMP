package com.retro99.reader.ui.audiobook

import androidx.compose.runtime.Composable

@Composable
expect fun AudiobookPlayerScreen(
    serverId: String,
    bookUuid: String,
    onClose: () -> Unit,
)
