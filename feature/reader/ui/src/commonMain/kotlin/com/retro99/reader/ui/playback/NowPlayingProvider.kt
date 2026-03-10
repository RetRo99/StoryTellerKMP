package com.retro99.reader.ui.playback

import com.retro99.books.domain.model.BookType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Data class representing the currently playing book information.
 * Used by the mini-player to show what's currently playing.
 */
data class NowPlayingInfo(
    val serverId: String,
    val bookUuid: String,
    val bookType: BookType,
    val bookTitle: String,
    val coverUrl: String?,
)

/**
 * Provider interface for now-playing state.
 *
 * This interface allows commonMain UI components to observe playback state
 * without depending on platform-specific implementations like MediaPlaybackController.
 *
 * Platform implementations:
 * - Android: AndroidNowPlayingProvider wraps MediaPlaybackController
 * - iOS: Stub implementation returning empty flows (audio playback not yet supported)
 */
interface NowPlayingProvider {

    /**
     * Flow of the currently playing book info.
     * Emits null when nothing is playing.
     */
    val nowPlayingInfo: StateFlow<NowPlayingInfo?>

    /**
     * Flow indicating whether audio is currently playing (not paused).
     */
    val isPlaying: StateFlow<Boolean>

    /**
     * Toggles play/pause state.
     */
    fun togglePlayPause()

    /**
     * Stops playback completely.
     */
    fun stop()
}

