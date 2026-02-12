package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.model.AudioLocatorState
import com.retro99.reader.ui.model.AudioPlaybackState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Controller interface for audio playback in ReadAloud books.
 *
 * This controller is injected into the ViewModel and provides:
 * - Playback control methods (play, pause, resume, seek, etc.)
 * - Unified playback state via [audioPlaybackState]
 * - Platform-agnostic API for audio playback
 *
 * Platform implementations:
 * - Android: Uses ExoPlayer with SMIL parsing
 * - iOS: Bridges to Swift MediaOverlayPlayer with AVPlayer
 */
interface AudioController : AutoCloseable {

    // State observation flows

    /**
     * Unified playback state for ReadAloud books.
     * Includes position, playing state, playback state, and readiness.
     */
    val audioPlaybackState: Flow<AudioPlaybackState>

    /**
     * Flow of playback state changes for ReadAloud books.
     * This includes states like PLAYING, PAUSED, BUFFERING, STOPPED, and ERROR.
     * Use this to show error feedback to the user when SMIL parsing fails or other errors occur.
     * Returns an empty flow if the book doesn't support media overlays.
     */
    val playbackState: Flow<PlaybackState>


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
     * Starts audio playback from a specific text fragment (sentence).
     * Used when user double-taps on a sentence to start playback from that point.
     *
     * @param fragmentId The fragment ID of the sentence (e.g., "chapter44.xhtml-sentence50")
     * @param chapterHref Optional chapter href. If null, uses the current chapter.
     */
    fun playFromFragment(fragmentId: String, chapterHref: String? = null)

    /**
     * Updates the audio position to match a given text fragment ID without starting playback.
     *
     * This is used when the user navigates while audio is not playing, so the seek bar
     * reflects where playback would start. The position is emitted through [audioPlaybackState].
     *
     * @param fragmentId The fragment ID of the sentence (e.g., "chapter44.xhtml-sentence50")
     */
    fun updatePositionForFragment(fragmentId: String)

    /**
     * Dismisses the permission denied dialog.
     */
    fun dismissPermissionDeniedDialog()

    fun onBookLocationChanged(locator: LocatorState)

    /**
     * Flow of current audio locator with timing information.
     * Emits whenever the audio playback moves to a new sentence.
     * Includes sentence duration for pre-emptive page turn calculations.
     */
    val currentAudioLocator: StateFlow<AudioLocatorState?>
}
