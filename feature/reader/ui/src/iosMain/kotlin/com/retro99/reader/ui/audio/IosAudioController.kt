package com.retro99.reader.ui.audio

import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.model.AudioPositionState
import com.retro99.reader.ui.model.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

/**
 * iOS implementation of [AudioController].
 * Delegates audio playback operations to the [EpubReaderBridge].
 */
@Single
class IosAudioController(
    private val bridge: EpubReaderBridge,
) : AudioController {

    private var mediaOverlaysInitialized = false

    // Internal mutable flows for state observation
    private val _audioPositionState = MutableSharedFlow<AudioPositionState>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    private val _isPlayingState = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    private val _isPlayerReady = MutableStateFlow(false)
    private val _showPermissionDeniedDialog = MutableStateFlow(false)
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)

    // AudioController state observation flows
    override val audioPositionState: Flow<AudioPositionState> = _audioPositionState
    override val isPlayingState: Flow<Boolean> = _isPlayingState
    override val isPlayerReady: Flow<Boolean> = _isPlayerReady
    override val playbackState: Flow<PlaybackState> = _playbackState

    // iOS doesn't require notification permission for audio playback
    override val showPermissionDeniedDialog: Flow<Boolean> = _showPermissionDeniedDialog

    // iOS doesn't need permission rationale - always false
    override val showPermissionRationale: Flow<Boolean> = flowOf(false)

    init {
        setupCallbacks()
    }

    /**
     * Initializes media overlays for ReadAloud books.
     * This should be called AFTER the navigator view controller is created.
     */
    fun initializeMediaOverlaysIfNeeded() {
        if (mediaOverlaysInitialized) {
            return
        }

        if (!bridge.hasMediaOverlays()) {
            // For non-readaloud books, mark as ready immediately
            _isPlayerReady.value = true
            return
        }

        bridge.initializeMediaOverlays {
            mediaOverlaysInitialized = true
            // The callback will trigger _isPlayerReady.value = true via setupCallbacks
        }
    }

    private fun setupCallbacks() {
        bridge.setOnPlaybackStateChangedCallback { state ->
            _audioPositionState.tryEmit(
                AudioPositionState(
                    currentPositionMs = state.currentPositionMs,
                    totalDurationMs = state.durationMs,
                ),
            )
            _isPlayingState.tryEmit(state.isPlaying)
            // Map isPlaying to PlaybackState enum
            _playbackState.value = if (state.isPlaying) {
                PlaybackState.PLAYING
            } else {
                PlaybackState.PAUSED
            }
        }

        bridge.setOnMediaPlayerReadyCallback {
            _isPlayerReady.value = true
        }
    }

    override fun playAudio(initialPositionMs: Long?) {
        ensureMediaOverlaysInitialized {
            bridge.playAudio(initialPositionMs)
        }
    }

    override fun resumeAudio() {
        bridge.resumeAudio()
    }

    override fun pauseAudio() {
        bridge.pauseAudio()
    }

    override fun seekToAudioPosition(timestampMs: Long) {
        bridge.seekToAudioPosition(timestampMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        bridge.setPlaybackSpeed(speed)
    }

    override fun skipForward() {
        bridge.skipForward()
    }

    override fun skipBackward() {
        bridge.skipBackward()
    }

    override fun dismissPermissionDeniedDialog() {
        _showPermissionDeniedDialog.value = false
    }

    private fun ensureMediaOverlaysInitialized(onReady: () -> Unit) {
        if (mediaOverlaysInitialized) {
            onReady()
            return
        }

        bridge.initializeMediaOverlays {
            mediaOverlaysInitialized = true
            onReady()
        }
    }

    override fun close() {
        bridge.setOnPlaybackStateChangedCallback(null)
        bridge.setOnMediaPlayerReadyCallback(null)
    }
}
