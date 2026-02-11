package com.retro99.reader.ui.reader

import com.retro99.base.ui.BaseIntent
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel

sealed interface ReaderIntent : BaseIntent {
    data class UpdateSettings(
        val settings: ReaderSettingsUiModel,
    ) : ReaderIntent

    data object ToggleSettings : ReaderIntent

    data object Close : ReaderIntent

    /**
     * Navigate to app settings screen.
     */
    data object OnSettingsClicked : ReaderIntent

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

    // Table of Contents intents

    /**
     * Toggle the table of contents visibility.
     */
    data object ToggleToc : ReaderIntent

    /**
     * Navigate to a specific chapter from the TOC.
     *
     * @param href The href of the chapter to navigate to
     * @param currentPosition The current position before navigation (for undo functionality)
     */
    data class GoToChapter(
        val href: String,
        val currentPosition: PositionUiModel?,
    ) : ReaderIntent

    /**
     * Undo the last chapter navigation and return to the previous position.
     *
     * @param position The position to navigate back to
     */
    data class UndoChapterNavigation(val position: PositionUiModel) : ReaderIntent

    /**
     * Dismiss the chapter navigation undo snackbar.
     */
    data object DismissChapterNavigationUndo : ReaderIntent

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
}

