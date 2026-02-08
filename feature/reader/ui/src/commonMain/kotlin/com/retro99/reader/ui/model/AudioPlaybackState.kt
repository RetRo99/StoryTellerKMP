package com.retro99.reader.ui.model

/**
 * Unified playback state for ReadAloud audio.
 *
 * @param currentPositionMs The current playback position in milliseconds, or null if not yet known
 * @param totalDurationMs The total duration of the current audio segment in milliseconds, or null if unknown
 * @param isPlaying Whether audio is currently playing
 * @param playbackState The detailed playback state
 * @param isPlayerReady Whether the media player is initialized and ready
 */
data class AudioPlaybackState(
    val currentPositionMs: Long?,
    val totalDurationMs: Long?,
    val isPlaying: Boolean,
    val playbackState: PlaybackState,
    val isPlayerReady: Boolean,
)