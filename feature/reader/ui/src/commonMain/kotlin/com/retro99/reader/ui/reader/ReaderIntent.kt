package com.retro99.reader.ui.reader

import com.retro99.base.ui.BaseIntent
import com.retro99.reader.ui.model.ReaderSettingsUiModel

sealed interface ReaderIntent : BaseIntent {
    data class UpdateSettings(
        val settings: ReaderSettingsUiModel,
    ) : ReaderIntent

    data object ToggleSettings : ReaderIntent

    data object Close : ReaderIntent

    /**
     * Navigate to the next page.
     */
    data object GoToNextPage : ReaderIntent

    /**
     * Navigate to the previous page.
     */
    data object GoToPreviousPage : ReaderIntent

    /**
     * User chose to use the local position when a conflict was detected.
     */
    data object UseLocalPosition : ReaderIntent

    /**
     * User chose to use the remote position when a conflict was detected.
     */
    data object UseRemotePosition : ReaderIntent

    // Media control intents for ReadAloud books

    /**
     * Toggle audio playback (play/pause).
     */
    data object TogglePlayback : ReaderIntent

    /**
     * Seek to a specific audio position.
     *
     * @param audioTimestampMs The audio position in milliseconds
     */
    data class SeekTo(val audioTimestampMs: Long) : ReaderIntent

    /**
     * Set the playback speed.
     *
     * @param speed The playback speed (e.g., 0.5, 1.0, 1.5, 2.0)
     */
    data class SetPlaybackSpeed(val speed: Float) : ReaderIntent

    /**
     * Skip forward by a specified amount.
     *
     * @param milliseconds The amount to skip forward in milliseconds
     */
    data class SkipForward(val milliseconds: Long = 10_000L) : ReaderIntent

    /**
     * Skip backward by a specified amount.
     *
     * @param milliseconds The amount to skip backward in milliseconds
     */
    data class SkipBackward(val milliseconds: Long = 10_000L) : ReaderIntent

    /**
     * Update the current audio position reported by the navigator.
     *
     * @param positionMs The current audio position in milliseconds
     * @param totalDurationMs The total duration of the audio in milliseconds
     */
    data class UpdateAudioPosition(
        val positionMs: Long,
        val totalDurationMs: Long?,
    ) : ReaderIntent

    /**
     * Update the playing state reported by the navigator.
     *
     * @param isPlaying Whether audio is currently playing
     */
    data class UpdatePlayingState(
        val isPlaying: Boolean,
    ) : ReaderIntent

    /**
     * Notify that the media player is ready for playback.
     */
    data object MediaPlayerReady : ReaderIntent
}

