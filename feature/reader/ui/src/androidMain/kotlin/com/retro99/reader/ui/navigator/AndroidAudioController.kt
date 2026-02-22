package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.di.InitialAudioPosition
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
    private val initialAudioPosition: InitialAudioPosition,
) : AudioController {

    private var currentBookLocation: LocatorState? = null

    private var controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Tracks whether playback has been started at least once.
     * Used to determine whether to start fresh (with positioning) or resume.
     */
    private var hasStartedPlayback = false

    override val currentAudioLocator: StateFlow<AudioLocatorState?>
        get() = locatorTracker.currentLocator

    override val audioPlaybackState: Flow<AudioPlaybackState>
        get() = combine(
            locatorTracker.normalizedPosition,  // Use normalized position for UI display (starts at 0:00)
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

    /**
     * Initial chapter href from saved reading progress.
     * Used for initializing media overlays at the correct chapter.
     */
    private val initialChapterHref: String? = initialAudioPosition.href

    init {
        controllerScope.launch {
            initializeMediaOverlays()
        }
    }

    private suspend fun initializeMediaOverlays() {
        val chapterHref = initialChapterHref
            ?: publication.publication.readingOrder.firstOrNull()?.href?.toString()
        val chapterUrl = chapterHref?.let { Url(it) }

        player.initialize(chapterHref)

        if (chapterUrl != null) {
            // Pass current position so audio is pre-buffered at the correct position
            // This makes playback start instantly when user clicks play
            player.prepareChapterDuration(chapterUrl, locatorTracker.currentPosition.value)
        }
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

    override fun resetPlaybackState() {
        // Only reset if not currently playing - when playing, the audio drives the state
        if (playbackStateTracker.isPlaying.value) return
        hasStartedPlayback = false
    }

    override fun setInitialAudioPosition(positionMs: Long?) {
        hasStartedPlayback = false
        // Update the LocatorTracker so the seek bar reflects the new position
        if (positionMs != null) {
            locatorTracker.setInitialPosition(positionMs)
        }
    }

    override fun pauseAudio() {
        player.pause()
    }

    /**
     * Starts playback from the current position shown on the seek bar.
     * The LocatorTracker maintains the position - either from saved state or user navigation.
     */
    private fun startPlaybackFromCurrentPosition() {
        val currentPosition = locatorTracker.currentPosition.value
        executePlayCommand(currentPosition)
        hasStartedPlayback = true
    }

    override fun seekToAudioPosition(timestampMs: Long) {
        // Convert normalized position (from UI) to raw ExoPlayer position
        val rawPosition = locatorTracker.normalizedToRawPosition(timestampMs)
        player.seekTo(rawPosition)
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

        // First, find the clip WITHOUT seeking ExoPlayer yet.
        // We need to check if audio file switch is needed before seeking,
        // because seeking to a position beyond the current file's duration
        // would cause issues.
        val matchingClip = locatorTracker.findClipForFragment(fragmentId)
        if (matchingClip == null) {
            return
        }

        val clipAudioHref = matchingClip.audioHref
        val currentAudioHref = player.getCurrentAudioHref()
        val positionMs = (matchingClip.startTime * 1000.0).toLong()

        if (clipAudioHref != currentAudioHref) {
            // Audio file switch needed - update internal position state,
            // but let switchAudioFileIfNeeded handle the actual seek
            locatorTracker.updatePositionForFragment(fragmentId, skipSeek = true)
            player.switchAudioFileIfNeeded(clipAudioHref, positionMs)
        } else {
            // Same audio file - update position and seek normally
            locatorTracker.updatePositionForFragment(fragmentId, skipSeek = false)
        }
    }

    override fun dismissPermissionDeniedDialog() {
        player.dismissPermissionDeniedDialog()
    }

    override fun onBookLocationChanged(locator: LocatorState, visibleSentenceId: String?) {
        val isChapterChange = currentBookLocation?.href != locator.href
        currentBookLocation = locator
        if (isChapterChange) {
            onChapterChanged(locator, visibleSentenceId)
        } else if (visibleSentenceId != null) {
            updatePositionForFragment(visibleSentenceId)
        }
        resetPlaybackState()
    }

    private fun onChapterChanged(locator: LocatorState, visibleSentenceId: String?) {
        val chapterUrl = Url(locator.href) ?: return
        // Get the visible fragment ID (sentence) so we can prepare the correct audio file
        // for chapters that span multiple audio files
        val fragmentId = locator.fragments?.firstOrNull()
        controllerScope.launch {
            player.prepareChapterDuration(chapterUrl, targetFragmentId = fragmentId)
            visibleSentenceId?.let { updatePositionForFragment(it) }
        }
    }

    override fun close() {
        player.release()
        controllerScope.cancel()
    }

    private fun executePlayCommand(initialPositionMs: Long?) {
        val currentLocator = currentBookLocation
        val currentChapterHref = currentLocator?.href?.let { Url(it) }
        val fragmentId = currentLocator?.fragments?.firstOrNull()
        val progression = currentLocator?.progression
        if (currentChapterHref == null) {
            player.play(initialPositionMs = initialPositionMs)
            return
        }
        player.play(currentChapterHref, fragmentId, progression, initialPositionMs)
    }
}