package com.retro99.reader.ui.navigator

import android.content.Context
import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.base.nowMillis
import com.retro99.reader.ui.media.MediaOverlayPlayer
import com.retro99.reader.ui.media.smil.SmilParser
import com.retro99.reader.ui.media.smil.SmilQuickScanner
import com.retro99.reader.ui.model.AudioLocatorState
import com.retro99.reader.ui.model.AudioPlaybackState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import com.retro99.reader.ui.playback.MediaPlaybackController
import com.retro99.reader.ui.playback.NotificationPermissionHandler
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import org.readium.r2.shared.util.Url

@OptIn(ExperimentalCoroutinesApi::class)
@Single(binds = [AudioController::class])
class AndroidAudioController(
    private val context: Context,
    private val analytics: Analytics,
    private val smilParser: SmilParser,
    private val quickScanner: SmilQuickScanner,
    private val mediaPlaybackController: MediaPlaybackController,
    private val notificationPermissionHandler: NotificationPermissionHandler,
) : AudioController {

    private var currentBookLocation: LocatorState? = null

    private val _mediaOverlayPlayer = MutableStateFlow<MediaOverlayPlayer?>(null)
    private val _currentAudioLocator = MutableStateFlow<AudioLocatorState?>(null)
    private var controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private sealed class PendingMediaCommand {
        data class Play(val initialPositionMs: Long?) : PendingMediaCommand()
        data object Resume : PendingMediaCommand()
        data object Pause : PendingMediaCommand()
        data class SeekTo(val timestampMs: Long) : PendingMediaCommand()
        data class SetSpeed(val speed: Float) : PendingMediaCommand()
    }

    private var pendingCommand: PendingMediaCommand? = null

    override val currentAudioLocator: StateFlow<AudioLocatorState?> = _currentAudioLocator

    override val audioPlaybackState: Flow<AudioPlaybackState>
        get() = _mediaOverlayPlayer
            .filterNotNull()
            .flatMapLatest { player ->
                combine(
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
            }
    override val playbackState: Flow<PlaybackState>
        get() = _mediaOverlayPlayer.flatMapLatest { player ->
            player?.playbackState ?: flowOf(PlaybackState.STOPPED)
        }
    override val showPermissionDeniedDialog: Flow<Boolean>
        get() = _mediaOverlayPlayer.flatMapLatest { player ->
            player?.showPermissionDeniedDialog ?: flowOf(false)
        }
    override val showPermissionRationale: Flow<Boolean>
        get() = _mediaOverlayPlayer.flatMapLatest { player ->
            player?.showPermissionRationale ?: flowOf(false)
        }

    private lateinit var publication: EpubPublication

    override suspend fun init(
        publication: EpubPublication,
    ) {
        // Recreate the scope if it was cancelled (e.g., after close() was called)
        if (!controllerScope.isActive) {
            controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }
        this.publication = publication
        initializeMediaOverlays(
            publication = publication,
        )
    }

    private suspend fun initializeMediaOverlays(publication: EpubPublication) {
        if (!publication.hasMediaOverlays) {
            return
        }

        val totalStartTime = nowMillis()
        logger.i { "⏱️ MediaOverlay initialization STARTED (lazy loading)" }

        val playerCreateStart = nowMillis()
        val player = MediaOverlayPlayer(
            context = context,
            publication = publication.publication,
            analytics = analytics,
            smilParser = smilParser,
            quickScanner = quickScanner,
            mediaPlaybackController = mediaPlaybackController,
            notificationPermissionHandler = notificationPermissionHandler,
        )

        val playerCreateTime = nowMillis() - playerCreateStart
        logger.i { "⏱️ MediaOverlayPlayer created in ${playerCreateTime}ms" }

        _mediaOverlayPlayer.value = player
        controllerScope.launch {
            player.currentLocator.collect { locator ->
                _currentAudioLocator.value = locator
            }
        }
        executePendingCommand(player)

        // Get initial chapter href for optimized index building
        val initialChapterHref = publication.initialPosition?.href
            ?: publication.publication.readingOrder.firstOrNull()?.href?.toString()
        val initialChapterUrl = initialChapterHref?.let { Url(it) }
            ?: publication.publication.readingOrder.firstOrNull()?.href?.toString()?.let { Url(it) }
        val initializeStart = nowMillis()
        player.initialize(initialChapterHref)
        val initializeTime = nowMillis() - initializeStart
        logger.i { "⏱️ player.initialize() (index building) completed in ${initializeTime}ms" }

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
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing Play command" }
            pendingCommand = PendingMediaCommand.Play(initialPositionMs)
            return
        }
        executePlayCommand(player, initialPositionMs)
    }

    override fun resumeAudio() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing Resume command" }
            pendingCommand = PendingMediaCommand.Resume
            return
        }
        player.resume()
    }

    override fun pauseAudio() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, clearing pending command (pause requested)" }
            pendingCommand = null
            return
        }
        player.pause()
    }

    override fun seekToAudioPosition(timestampMs: Long) {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing SeekTo command: $timestampMs" }
            pendingCommand = PendingMediaCommand.SeekTo(timestampMs)
            return
        }
        player.seekTo(timestampMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing SetSpeed command: $speed" }
            pendingCommand = PendingMediaCommand.SetSpeed(speed)
            return
        }
        player.setPlaybackSpeed(speed)
    }

    override fun skipForward() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, ignoring skipForward" }
            return
        }
        player.seekForward()
    }

    override fun skipBackward() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, ignoring skipBackward" }
            return
        }
        player.seekBackward()
    }

    override fun dismissPermissionDeniedDialog() {
        _mediaOverlayPlayer.value?.dismissPermissionDeniedDialog()
    }

    override fun onBookLocationChanged(locator: LocatorState) {
        if (currentBookLocation?.href == locator.href) {
            currentBookLocation = locator
            return
        }
        currentBookLocation = locator
        _mediaOverlayPlayer.value?.let { player ->
            val chapterUrl = Url(locator.href) ?: return
            controllerScope.launch {
                player.prepareChapterDuration(chapterUrl)
            }

        }
    }

    override fun close() {
        logger.i { "AndroidAudioController RELEASE called" }
        val player = _mediaOverlayPlayer.value
        if (player != null) {
            logger.i { "Releasing MediaOverlayPlayer..." }
            player.release()
        } else {
            logger.w { "MediaOverlayPlayer was NULL - nothing to release" }
        }
        _mediaOverlayPlayer.value = null
        controllerScope.cancel()
    }

    private fun executePlayCommand(player: MediaOverlayPlayer, initialPositionMs: Long?) {
        logger.i { "🎵 MediaOverlayPlayer is available" }
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

    private fun executePendingCommand(player: MediaOverlayPlayer) {
        val command = pendingCommand ?: return
        pendingCommand = null

        logger.i { "🎵 Executing pending command: $command" }
        when (command) {
            is PendingMediaCommand.Play -> executePlayCommand(player, command.initialPositionMs)
            is PendingMediaCommand.Resume -> player.resume()
            is PendingMediaCommand.Pause -> player.pause()
            is PendingMediaCommand.SeekTo -> player.seekTo(command.timestampMs)
            is PendingMediaCommand.SetSpeed -> player.setPlaybackSpeed(command.speed)
        }
    }

    private companion object {
        private val logger = Logger.withTag("čič")
    }
}