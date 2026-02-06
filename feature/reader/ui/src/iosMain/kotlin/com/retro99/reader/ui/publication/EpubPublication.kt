package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.bridge.EpubReaderSettings
import com.retro99.reader.ui.model.AudioPositionState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.navigator.EpubNavigatorController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

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

    private var mediaOverlaysInitialized = false

    // Internal mutable flows for state observation
    private val _currentLocator = MutableSharedFlow<LocatorState>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    private val _audioPositionState = MutableSharedFlow<AudioPositionState>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    private val _isPlayingState = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    private val _isPlayerReady = MutableStateFlow(false)

    // EpubNavigatorController state observation flows
    override val currentLocator: Flow<LocatorState> = _currentLocator
    override val audioPositionState: Flow<AudioPositionState> = _audioPositionState
    override val isPlayingState: Flow<Boolean> = _isPlayingState
    override val isPlayerReady: Flow<Boolean> = _isPlayerReady

    init {
        setupCallbacks()
    }

    /**
     * Initializes media overlays for ReadAloud books.
     * This should be called AFTER the navigator view controller is created,
     * because the Swift MediaOverlayPlayer needs access to the navigator's current location.
     *
     * On Android, this happens in AndroidEpubNavigatorController.init which is created
     * after the navigator fragment exists. On iOS, we need to call this explicitly
     * after createReaderViewController returns.
     */
    fun initializeMediaOverlaysIfNeeded() {
        if (mediaOverlaysInitialized) {
            return
        }

        if (!hasMediaOverlays) {
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
        bridge.setOnPositionChangedCallback { locator ->
            _currentLocator.tryEmit(
                LocatorState(
                    href = locator.href,
                    type = locator.type,
                    title = locator.title,
                    progression = locator.progression,
                    position = locator.position,
                    totalProgression = locator.totalProgression,
                ),
            )
        }

        bridge.setOnPlaybackStateChangedCallback { state ->
            _audioPositionState.tryEmit(
                AudioPositionState(
                    currentPositionMs = state.currentPositionMs,
                    totalDurationMs = state.durationMs,
                ),
            )
            _isPlayingState.tryEmit(state.isPlaying)
        }

        bridge.setOnMediaPlayerReadyCallback {
            _isPlayerReady.value = true
        }
    }

    /**
     * Whether this publication has media overlays (audio narration).
     */
    actual val hasMediaOverlays: Boolean
        get() = bridge.hasMediaOverlays()

    /**
     * Closes the publication and releases resources.
     */
    actual fun close() {
        // Clear callbacks to prevent memory leaks
        bridge.setOnPositionChangedCallback(null)
        bridge.setOnPlaybackStateChangedCallback(null)
        bridge.setOnMediaPlayerReadyCallback(null)
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
}

