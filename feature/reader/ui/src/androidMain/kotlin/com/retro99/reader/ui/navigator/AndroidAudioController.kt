package com.retro99.reader.ui.navigator

import co.touchlab.kermit.Logger
import com.retro99.base.nowMillis
import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.media.MediaOverlayPlayer
import com.retro99.reader.ui.model.AudioLocatorState
import com.retro99.reader.ui.model.AudioPlaybackState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.readium.r2.shared.util.Url

@Scope(ReaderScope::class)
@Scoped(binds = [AudioController::class])
class AndroidAudioController(
    private val publication: EpubPublication,
    private val player: MediaOverlayPlayer,
) : AudioController {

    private var currentBookLocation: LocatorState? = null

    private val _currentAudioLocator = MutableStateFlow<AudioLocatorState?>(null)
    private var controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val currentAudioLocator: StateFlow<AudioLocatorState?> = _currentAudioLocator

    override val audioPlaybackState: Flow<AudioPlaybackState>
        get() = combine(
            player.currentPosition,
            player.totalDuration,
            player.isPlaying,
            player.playbackState,
        ) { positionMs, durationMs, isPlaying, playbackState ->
            AudioPlaybackState(
                currentPositionMs = positionMs,
                totalDurationMs = durationMs,
                isPlaying = isPlaying,
                playbackState = playbackState,
                isPlayerReady = true,
            )
        }

    override val playbackState: Flow<PlaybackState>
        get() = player.playbackState

    override val showPermissionDeniedDialog: Flow<Boolean>
        get() = player.showPermissionDeniedDialog

    override val showPermissionRationale: Flow<Boolean>
        get() = player.showPermissionRationale

    init {
        controllerScope.launch {
            initializeMediaOverlays()
        }
        controllerScope.launch {
            player.currentLocator.collect { locator ->
                _currentAudioLocator.value = locator
            }
        }
    }

    private suspend fun initializeMediaOverlays() {
        val totalStartTime = nowMillis()
        logger.i { "⏱️ MediaOverlay initialization STARTED" }

        val initialChapterHref = publication.initialPosition?.href
            ?: publication.publication.readingOrder.firstOrNull()?.href?.toString()
        val initialChapterUrl = initialChapterHref?.let { Url(it) }

        val initializeStart = nowMillis()
        player.initialize(initialChapterHref)
        val initializeTime = nowMillis() - initializeStart
        logger.i { "⏱️ player.initialize() completed in ${initializeTime}ms" }

        val chapterPrepareStart = nowMillis()
        if (initialChapterUrl != null) {
            player.prepareChapterDuration(initialChapterUrl)
        }
        val chapterPrepareTime = nowMillis() - chapterPrepareStart
        logger.i { "⏱️ prepareChapterDuration() completed in ${chapterPrepareTime}ms" }

        val totalTime = nowMillis() - totalStartTime
        logger.i { "⏱️ MediaOverlay initialization COMPLETE - TOTAL: ${totalTime}ms" }
    }

    override fun playAudio(initialPositionMs: Long?) {
        logger.i { "🎵 playAudio() called - initialPositionMs=$initialPositionMs" }
        executePlayCommand(initialPositionMs)
    }

    override fun resumeAudio() {
        player.resume()
    }

    override fun pauseAudio() {
        player.pause()
    }

    override fun seekToAudioPosition(timestampMs: Long) {
        player.seekTo(timestampMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    override fun skipForward() {
        player.seekForward()
    }

    override fun skipBackward() {
        player.seekBackward()
    }

    override fun dismissPermissionDeniedDialog() {
        player.dismissPermissionDeniedDialog()
    }

    override fun onBookLocationChanged(locator: LocatorState) {
        if (currentBookLocation?.href == locator.href) {
            currentBookLocation = locator
            return
        }
        currentBookLocation = locator
        val chapterUrl = Url(locator.href) ?: return
        controllerScope.launch {
            player.prepareChapterDuration(chapterUrl)
        }
    }

    override fun close() {
        logger.i { "AndroidAudioController RELEASE called" }
        player.release()
        controllerScope.cancel()
    }

    private fun executePlayCommand(initialPositionMs: Long?) {
        val currentLocator = currentBookLocation
        val currentChapterHref = currentLocator?.href?.let { Url(it) }
        val fragmentId = currentLocator?.fragments?.firstOrNull()
        val progression = currentLocator?.progression
        logger.i {
            "🎵 Calling player.play() - href=$currentChapterHref, " +
                    "fragmentId=$fragmentId, progression=$progression"
        }
        if (currentChapterHref == null) {
            player.play(initialPositionMs = initialPositionMs)
            return
        }
        player.play(currentChapterHref, fragmentId, progression, initialPositionMs)
    }

    private companion object {
        private val logger = Logger.withTag("AndroidAudioController")
    }
}