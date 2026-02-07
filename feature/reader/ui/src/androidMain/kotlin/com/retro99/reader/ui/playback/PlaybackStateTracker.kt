package com.retro99.reader.ui.playback

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.model.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Authoritative source for playback state, with support for optimistic updates
 * from the playback orchestrator.
 *
 * ## State Management Pattern
 *
 * This class uses an **optimistic update pattern** for state management:
 *
 * 1. **Optimistic writes**: [MediaOverlayPlayer] calls [setPlayingState] and [setPlaybackState]
 *    immediately when starting playback to provide instant UI feedback (e.g., show buffering spinner).
 *
 * 2. **Authoritative writes**: The [Player.Listener] callbacks update state based on actual
 *    ExoPlayer events, which may confirm or override the optimistic state.
 *
 * 3. **Rollback on failure**: If permission/focus/service fails, [MediaOverlayPlayer] resets
 *    state back to STOPPED/false.
 *
 * This means consumers of [isPlaying] and [playbackState] may see transient state sequences like:
 * - `false -> true -> false` (optimistic set, then permission denied)
 * - `STOPPED -> BUFFERING -> STOPPED` (optimistic set, then focus failed)
 * - `false -> true -> true` (optimistic set, then listener confirms - no-op due to StateFlow)
 *
 * Consumers should handle these transitions gracefully. StateFlow's conflation means
 * duplicate values won't emit, but rapid changes will.
 *
 * @param player The ExoPlayer instance to track
 * @param analytics Analytics for logging errors
 * @param onPlaybackEnded Callback when playback ends (STATE_ENDED)
 * @param onPlayerReady Callback when player is ready with duration and pending seek position
 * @param onIsPlayingChanged Callback when isPlaying changes (for position updates)
 */
class PlaybackStateTracker(
    private val player: ExoPlayer,
    private val analytics: Analytics,
    private val onPlaybackEnded: () -> Unit,
    private val onPlayerReady: (duration: Long, pendingSeekPosition: Long?) -> Unit,
    private val isPlayingChanged: (isPlaying: Boolean) -> Unit,
    private val onPlayerError: () -> Unit,
) {
    private val logger = Logger.withTag("PlaybackStateTracker")

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _totalDuration = MutableStateFlow<Long?>(null)
    val totalDuration: StateFlow<Long?> = _totalDuration.asStateFlow()

    // Pending initial position to seek to after audio is prepared
    private var pendingInitialPositionMs: Long? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            isPlayingChanged(isPlaying)
        }

        override fun onPlaybackStateChanged(playerState: Int) {
            val stateName = when (playerState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN($playerState)"
            }
            logger.i { "ExoPlayer onPlaybackStateChanged: $stateName, isPlaying=${player.isPlaying}" }
            updatePlaybackState(playerState, player.isPlaying)

            when (playerState) {
                Player.STATE_ENDED -> {
                    _isPlaying.value = false
                    onPlaybackEnded()
                }

                Player.STATE_READY -> {
                    val duration = player.duration
                    logger.i { "ExoPlayer READY - duration=${duration}ms, playWhenReady=${player.playWhenReady}" }
                    if (duration > 0) {
                        _totalDuration.value = duration
                    }
                    // Notify with pending position and clear it
                    onPlayerReady(duration, pendingInitialPositionMs)
                    pendingInitialPositionMs = null

                    // NOTE: Removed force-play hack that called player.play() when
                    // playWhenReady && !isPlaying. This caused loops with audio focus:
                    // 1. Focus lost -> player pauses
                    // 2. STATE_READY fires -> force play() called
                    // 3. play() triggers focus request -> may fail or conflict
                    // ExoPlayer handles playWhenReady correctly; trust it.
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            logger.e { "ExoPlayer ERROR: ${error.message}" }
            analytics.logException(error, "ExoPlayer playback error")
            // Update state to reflect the error so UI can show appropriate feedback
            _playbackState.value = PlaybackState.ERROR
            _isPlaying.value = false
            // Notify caller to clean up (stop foreground service, abandon focus, etc.)
            onPlayerError()
        }
    }

    init {
        player.addListener(playerListener)
    }

    /**
     * Sets the pending initial position to seek to after audio is prepared.
     */
    fun setPendingSeekPosition(positionMs: Long?) {
        pendingInitialPositionMs = positionMs
    }

    /**
     * Sets the playing state immediately.
     * Used when starting playback to ensure state change emissions work correctly.
     */
    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    /**
     * Sets the playback state immediately.
     */
    fun setPlaybackState(state: PlaybackState) {
        _playbackState.value = state
    }

    /**
     * Updates the total duration (used when calculating from SMIL clips).
     */
    fun setTotalDuration(durationMs: Long) {
        _totalDuration.value = durationMs
    }

    private fun updatePlaybackState(playerState: Int, isPlaying: Boolean) {
        _playbackState.value = when {
            isPlaying -> PlaybackState.PLAYING
            playerState == Player.STATE_BUFFERING -> PlaybackState.BUFFERING
            playerState == Player.STATE_ENDED -> PlaybackState.STOPPED
            playerState == Player.STATE_IDLE -> PlaybackState.STOPPED
            else -> PlaybackState.PAUSED
        }
    }

    /**
     * Removes the player listener. Call when releasing the player.
     */
    fun release() {
        player.removeListener(playerListener)
        _playbackState.value = PlaybackState.STOPPED
    }
}

