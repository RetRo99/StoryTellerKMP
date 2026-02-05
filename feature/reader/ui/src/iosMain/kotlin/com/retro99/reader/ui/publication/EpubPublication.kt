package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.bridge.EpubReaderSettings
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.navigator.EpubNavigatorController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS implementation of EpubPublication.
 * Wraps the EpubReaderBridge which provides access to the Swift Readium implementation.
 *
 * This class implements [EpubNavigatorController] directly, eliminating the need for
 * a separate navigator wrapper. Since it already holds the bridge, it can delegate
 * all navigation operations directly.
 */
actual class EpubPublication(
    internal val bridge: EpubReaderBridge,
    actual val initialSettings: ReaderSettingsUiModel,
    actual val bookType: BookType = BookType.EBOOK,
    internal val initialPosition: PositionUiModel?,
) : EpubNavigatorController {

    // Media playback state
    private val _isPlaying = MutableStateFlow(false)
    private val _currentAudioPosition = MutableStateFlow(0L)
    private val _totalDuration = MutableStateFlow<Long?>(null)
    private var mediaOverlaysInitialized = false

    /**
     * Whether this publication has media overlays (audio narration).
     */
    actual val hasMediaOverlays: Boolean
        get() = bridge.hasMediaOverlays()

    /**
     * Closes the publication and releases resources.
     */
    actual fun close() {
        bridge.closePublication()
    }

    // EpubNavigatorController implementation - delegates to bridge

    override fun goToNextPage() {
        bridge.goToNextPage()
    }

    override fun goToPreviousPage() {
        bridge.goToPreviousPage()
    }

    override fun goToChapter(href: String) {
        bridge.goToChapter(href)
    }

    override fun setSettings(settings: ReaderSettingsUiModel) {
        bridge.setSettings(settings = EpubReaderSettings.from(settings))
    }

    override fun goToPosition(position: PositionUiModel) {
        bridge.goToPosition(
            href = position.href,
            type = position.type,
            progression = position.progression,
            position = position.position,
        )
    }

    // Media playback implementation

    override val isPlaying: StateFlow<Boolean>
        get() = _isPlaying.asStateFlow()

    override val currentAudioPosition: StateFlow<Long>
        get() = _currentAudioPosition.asStateFlow()

    override val totalDuration: StateFlow<Long?>
        get() = _totalDuration.asStateFlow()

    override fun playAudio(initialPositionMs: Long?) {
        ensureMediaOverlaysInitialized {
            bridge.playAudio(initialPositionMs)
        }
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

    private fun ensureMediaOverlaysInitialized(onReady: () -> Unit) {
        if (mediaOverlaysInitialized) {
            onReady()
            return
        }

        bridge.setOnPlaybackStateChangedCallback { state ->
            _isPlaying.value = state.isPlaying
            _currentAudioPosition.value = state.currentPositionMs
            state.durationMs?.let { _totalDuration.value = it }
        }

        bridge.initializeMediaOverlays {
            mediaOverlaysInitialized = true
            onReady()
        }
    }
}

