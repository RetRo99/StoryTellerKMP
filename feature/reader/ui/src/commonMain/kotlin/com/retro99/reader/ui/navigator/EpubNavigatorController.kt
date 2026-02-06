package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.model.AudioPositionState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import kotlinx.coroutines.flow.Flow

/**
 * Controller interface for EPUB navigation and settings.
 *
 * This controller is owned by the View layer and handles:
 * - Page navigation (next/previous)
 * - Chapter navigation
 * - Reader settings application
 * - Media playback for ReadAloud books
 * - State observation via flows
 *
 * Platform implementations wrap the native navigator components:
 * - Android: [EpubNavigatorFragment] from Readium
 * - iOS: EPUBNavigatorViewController via bridge
 *
 * The View creates this controller after the publication is ready and
 * uses it to execute navigation commands from the ViewModel.
 */
interface EpubNavigatorController {

    // State observation flows

    /**
     * Flow of current reading position/locator changes.
     * Emits whenever the user navigates to a new position.
     */
    val currentLocator: Flow<LocatorState>

    /**
     * Flow of audio position updates for ReadAloud books.
     * Emits on every position change from the media player.
     * Returns an empty flow if the book doesn't support media overlays.
     */
    val audioPositionState: Flow<AudioPositionState>

    /**
     * Flow of playing state changes for ReadAloud books.
     * Returns an empty flow if the book doesn't support media overlays.
     */
    val isPlayingState: Flow<Boolean>

    /**
     * Flow that emits true when the media player is ready.
     * Returns a flow that emits false if the book doesn't support media overlays.
     */
    val isPlayerReady: Flow<Boolean>

    /**
     * Navigates to the next page.
     */
    fun goToNextPage()

    /**
     * Navigates to the previous page.
     */
    fun goToPreviousPage()

    /**
     * Navigates to a specific chapter by its href.
     *
     * @param href The href of the chapter to navigate to
     */
    fun goToChapter(href: String)

    /**
     * Applies the given reader settings.
     *
     * @param settings The reader settings to apply
     */
    fun setSettings(settings: ReaderSettingsUiModel)

    /**
     * Navigates to a specific position in the publication.
     *
     * @param position The position to navigate to
     */
    fun goToPosition(position: PositionUiModel)

    // Media playback methods for ReadAloud books

    /**
     * Starts audio playback, optionally seeking to a specific position.
     *
     * @param initialPositionMs Optional initial position in milliseconds to seek to before playing.
     *                          If null, playback starts from the current text position.
     */
    fun playAudio(initialPositionMs: Long? = null)

    /**
     * Resumes audio playback from the current position without seeking.
     */
    fun resumeAudio()

    /**
     * Pauses audio playback.
     */
    fun pauseAudio()

    /**
     * Seeks to a specific audio position.
     *
     * @param timestampMs The position in milliseconds
     */
    fun seekToAudioPosition(timestampMs: Long)

    /**
     * Sets the playback speed.
     *
     * @param speed The playback speed (e.g., 0.5, 1.0, 1.5, 2.0)
     */
    fun setPlaybackSpeed(speed: Float)
}

