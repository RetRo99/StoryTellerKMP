package com.retro99.reader.ui.reader

import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel

/**
 * Commands emitted by the ViewModel for the View to execute.
 *
 * This sealed class represents navigation and settings commands that the ViewModel
 * needs to communicate to the View. The View collects these commands via a SharedFlow
 * and executes them using its [EpubNavigatorController].
 *
 * This pattern maintains unidirectional data flow:
 * - ViewModel tells *what* to do (emits commands)
 * - View decides *how* to do it (executes using platform navigator)
 */
sealed class ReaderCommand {

    /**
     * Command to navigate to the next page.
     */
    data object GoToNextPage : ReaderCommand()

    /**
     * Command to navigate to the previous page.
     */
    data object GoToPreviousPage : ReaderCommand()

    /**
     * Command to navigate to a specific chapter.
     *
     * @param href The href of the chapter to navigate to
     */
    data class GoToChapter(val href: String) : ReaderCommand()

    /**
     * Command to apply reader settings.
     *
     * @param settings The reader settings to apply
     */
    data class ApplySettings(val settings: ReaderSettingsUiModel) : ReaderCommand()

    /**
     * Command to navigate to a specific position.
     * Used when resolving position conflicts.
     *
     * @param position The position to navigate to
     */
    data class GoToPosition(val position: PositionUiModel) : ReaderCommand()

    // Media playback commands for ReadAloud books

    /**
     * Command to start audio playback.
     *
     * @param initialPositionMs Optional initial position in milliseconds
     */
    data class StartPlayback(val initialPositionMs: Long? = null) : ReaderCommand()

    /**
     * Command to pause audio playback.
     */
    data object PausePlayback : ReaderCommand()

    /**
     * Command to resume audio playback.
     */
    data object ResumePlayback : ReaderCommand()

    /**
     * Command to seek to a specific audio position.
     *
     * @param audioTimestampMs The audio position in milliseconds
     */
    data class SeekToAudioPosition(val audioTimestampMs: Long) : ReaderCommand()

    /**
     * Command to set the playback speed.
     *
     * @param speed The playback speed (e.g., 0.5, 1.0, 1.5, 2.0)
     */
    data class SetPlaybackSpeed(val speed: Float) : ReaderCommand()

    /**
     * Command to skip forward by a fixed increment (10 seconds).
     * Uses the player's authoritative position rather than ViewModel state.
     */
    data object SkipForward : ReaderCommand()

    /**
     * Command to skip backward by a fixed increment (10 seconds).
     * Uses the player's authoritative position rather than ViewModel state.
     */
    data object SkipBackward : ReaderCommand()
}

