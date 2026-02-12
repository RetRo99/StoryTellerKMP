package com.retro99.reader.ui.navigator

import co.touchlab.kermit.Logger
import com.retro99.base.nowMillis
import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.media.MediaOverlayPlayer
import com.retro99.reader.ui.model.AudioLocatorState
import com.retro99.reader.ui.model.AudioPlaybackState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import com.retro99.reader.ui.playback.LocatorTracker
import com.retro99.reader.ui.playback.PlaybackStateTracker
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.readium.r2.shared.util.Url

@Scope(ReaderScope::class)
@Scoped(binds = [AudioController::class])
class AndroidAudioController(
    private val publication: EpubPublication,
    private val player: MediaOverlayPlayer,
    private val playbackStateTracker: PlaybackStateTracker,
    private val locatorTracker: LocatorTracker,
) : AudioController {

    private var currentBookLocation: LocatorState? = null

    private var controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Tracks whether playback has been started at least once.
     * Used to determine whether to start fresh (with positioning) or resume.
     */
    private var hasStartedPlayback = false

    /**
     * Initial audio position from saved reading progress.
     * Used on first playback, then cleared.
     * Initialized from publication's saved position.
     */
    private var initialPositionMs: Long? = publication.initialPosition?.audioTimestampMs

    /**
     * Currently visible sentence ID, updated by the sync coordinator.
     * Used for precise positioning when starting playback.
     */
    private var currentVisibleSentenceId: String? = null

    override val currentAudioLocator: StateFlow<AudioLocatorState?>
        get() = locatorTracker.currentLocator

    override val audioPlaybackState: Flow<AudioPlaybackState>
        get() = combine(
            locatorTracker.currentPosition,
            playbackStateTracker.totalDuration,
            playbackStateTracker.isPlaying,
            playbackStateTracker.playbackState,
            playbackStateTracker.isPlayerReady,
        ) { positionMs, durationMs, isPlaying, playbackState, isPlayerReady ->
            AudioPlaybackState(
                currentPositionMs = positionMs,
                totalDurationMs = durationMs,
                isPlaying = isPlaying,
                playbackState = playbackState,
                isPlayerReady = isPlayerReady,
            )
        }

    override val playbackState: Flow<PlaybackState>
        get() = playbackStateTracker.playbackState

    override val showPermissionDeniedDialog: Flow<Boolean>
        get() = player.showPermissionDeniedDialog

    override val showPermissionRationale: Flow<Boolean>
        get() = player.showPermissionRationale

    override val chapterAudioCompleted: Flow<String>
        get() = playbackStateTracker.chapterAudioCompleted.map {
            // Return the current chapter href when audio completes
            currentBookLocation?.href ?: ""
        }.filter { it.isNotEmpty() }

    init {
        controllerScope.launch {
            initializeMediaOverlays()
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
            // Pass initial position so audio is pre-buffered at the correct position
            // This makes playback start instantly when user clicks play
            player.prepareChapterDuration(initialChapterUrl, initialPositionMs)
        }
        val chapterPrepareTime = nowMillis() - chapterPrepareStart
        logger.i { "⏱️ prepareChapterDuration() completed in ${chapterPrepareTime}ms" }

        val totalTime = nowMillis() - totalStartTime
        logger.i { "⏱️ MediaOverlay initialization COMPLETE - TOTAL: ${totalTime}ms" }
    }

    override fun togglePlayback() {
        val isCurrentlyPlaying = playbackStateTracker.isPlaying.value
        if (isCurrentlyPlaying) {
            player.pause()
        } else {
            if (!hasStartedPlayback) {
                startPlaybackFromCurrentPosition()
            } else {
                player.resume()
            }
        }
    }

    override fun setVisibleSentenceId(sentenceId: String?) {
        currentVisibleSentenceId = sentenceId
    }

    override fun resetPlaybackState() {
        // Only reset if not currently playing - when playing, the audio drives the state
        if (playbackStateTracker.isPlaying.value) return
        hasStartedPlayback = false
        initialPositionMs = null
    }

    override fun pauseAudio() {
        player.pause()
    }

    /**
     * Starts playback from the current position.
     * Uses saved position if available, otherwise uses the cached visible sentence.
     */
    private fun startPlaybackFromCurrentPosition() {
        val savedPosition = initialPositionMs
        if (savedPosition != null) {
            logger.i { "🎵 Starting playback from saved position: ${savedPosition}ms" }
            executePlayCommand(savedPosition)
        } else {
            val visibleSentenceId = currentVisibleSentenceId
            if (visibleSentenceId != null) {
                logger.i { "🎵 Starting playback from visible sentence: $visibleSentenceId" }
                playFromFragment(visibleSentenceId, chapterHref = null)
            } else {
                logger.i { "🎵 Starting playback from beginning (no position info)" }
                executePlayCommand(null)
            }
        }
        hasStartedPlayback = true
        initialPositionMs = null // Clear after first use
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

    override fun playFromFragment(fragmentId: String, chapterHref: String?) {
        logger.i { "🎵 playFromFragment() called - fragmentId=$fragmentId, chapterHref=$chapterHref" }
        val href = chapterHref?.let { Url(it) } ?: currentBookLocation?.href?.let { Url(it) }
        player.play(
            chapterHref = href,
            initialFragmentId = fragmentId,
            initialProgression = null,
            initialPositionMs = null,
        )
    }

    override fun updatePositionForFragment(fragmentId: String) {
        // Only update position when not playing - when playing, the position
        // is driven by the audio playback itself
        if (playbackStateTracker.isPlaying.value) return
        locatorTracker.updatePositionForFragment(fragmentId)
    }

    override fun dismissPermissionDeniedDialog() {
        player.dismissPermissionDeniedDialog()
    }

    override fun onBookLocationChanged(locator: LocatorState) {
        val isChapterChange = currentBookLocation?.href != locator.href
        currentBookLocation = locator
        if (isChapterChange) {
            onChapterChanged(locator)
        }
    }

    private fun onChapterChanged(locator: LocatorState) {
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