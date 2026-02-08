package com.retro99.reader.ui.audio

import com.retro99.reader.ui.model.AudioPositionState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Controller interface for audio playback in ReadAloud books.
 *
 * This controller is injected into the ViewModel and provides:
 * - Playback control methods (play, pause, resume, seek, etc.)
 * - Unified playback state via [audioUiState]
 * - Platform-agnostic API for audio playback
 *
 * Platform implementations:
 * - Android: Uses ExoPlayer with SMIL parsing
 * - iOS: Bridges to Swift MediaOverlayPlayer with AVPlayer
 */
interface AudioController : AutoCloseable {

    // State observation flows

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
     * Flow of playback state changes for ReadAloud books.
     * This includes states like PLAYING, PAUSED, BUFFERING, STOPPED, and ERROR.
     * Use this to show error feedback to the user when SMIL parsing fails or other errors occur.
     * Returns an empty flow if the book doesn't support media overlays.
     */
    val playbackState: Flow<PlaybackState>

    /**
     * Flow that emits true when the media player is ready.
     * Returns a flow that emits false if the book doesn't support media overlays.
     */
    val isPlayerReady: Flow<Boolean>

    /**
     * Flow that emits true when the notification permission was denied
     * and a dialog should be shown to the user.
     *
     * **Platform behavior:**
     * - **Android**: Emits true when POST_NOTIFICATIONS permission is denied (Android 13+).
     *   The dialog prompts the user to open app settings.
     * - **iOS**: Always emits false (no-op). iOS doesn't require notification permission
     *   for audio playback, so this flow is included only for API parity.
     *
     * Returns an empty flow if the book doesn't support media overlays.
     */
    val showPermissionDeniedDialog: Flow<Boolean>

    /**
     * Flow that emits true when the permission denial dialog should show a rationale
     * (user can be asked again) vs directing to settings (permanently denied).
     *
     * **Platform behavior:**
     * - **Android**: Emits true when user denied but can be asked again.
     * - **iOS**: Always emits false (no-op).
     */
    val showPermissionRationale: Flow<Boolean>

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

    /**
     * Skips forward by a fixed increment (10 seconds).
     * Uses the player's authoritative position rather than ViewModel state.
     */
    fun skipForward()

    /**
     * Skips backward by a fixed increment (10 seconds).
     * Uses the player's authoritative position rather than ViewModel state.
     */
    fun skipBackward()

    /**
     * Dismisses the permission denied dialog.
     */
    fun dismissPermissionDeniedDialog()

    fun onBookLocationChanged(locator: LocatorState)
    suspend fun init(publication: EpubPublication)
    val currentAudioLocator: StateFlow<LocatorState?>
}
