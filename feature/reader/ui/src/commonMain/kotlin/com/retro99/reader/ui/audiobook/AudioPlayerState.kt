package com.retro99.reader.ui.audiobook

data class AudioPlayerState(
    val bookUuid: String = "",
    val bookTitle: String = "",
    val bookCoverUrl: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isLoading: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val currentTrackIndex: Int = 0,
    val trackCount: Int = 0,
    val trackTitles: List<String> = emptyList(),
    val playbackSpeed: Float = 1.0f,
    val error: String? = null,
)

data class AudioPlayerCallbacks(
    val onPlayPause: () -> Unit,
    val onSkipForward: () -> Unit,
    val onSkipBackward: () -> Unit,
    val onPreviousTrack: () -> Unit,
    val onNextTrack: () -> Unit,
    val onSeek: (Long) -> Unit,
    val onSelectTrack: (Int) -> Unit,
    val onSpeedChange: (Float) -> Unit,
    val onClose: () -> Unit,
)
