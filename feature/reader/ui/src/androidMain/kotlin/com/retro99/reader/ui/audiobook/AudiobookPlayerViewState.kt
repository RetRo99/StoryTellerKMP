package com.retro99.reader.ui.audiobook

import androidx.media3.common.Player

data class AudiobookPlayerViewState(
    val bookTitle: String = "",
    val bookCoverUrl: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val currentTrackIndex: Int = 0,
    val trackCount: Int = 0,
    val trackTitles: List<String> = emptyList(),
    val playbackSpeed: Float = 1.0f,
    val playbackState: Int = Player.STATE_IDLE,
    val error: String? = null,
)
