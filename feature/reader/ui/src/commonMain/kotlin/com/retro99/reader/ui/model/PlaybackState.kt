package com.retro99.reader.ui.model

/**
 * Centralized playback state for audio playback.
 * Used across platforms to track the current state of the media player.
 */
enum class PlaybackState {
    /**
     * Audio is currently playing.
     */
    PLAYING,

    /**
     * Audio is paused by user action.
     */
    PAUSED,

    /**
     * Audio is buffering/loading.
     */
    BUFFERING,

    /**
     * Audio playback is stopped (no active session).
     */
    STOPPED,

    /**
     * Audio playback encountered an error.
     */
    ERROR,
}

