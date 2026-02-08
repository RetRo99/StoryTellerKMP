package com.retro99.reader.ui.navigator

import android.content.Context
import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.base.nowMillis
import com.retro99.reader.ui.audio.AudioController
import com.retro99.reader.ui.media.MediaOverlayPlayer
import com.retro99.reader.ui.media.smil.SmilParser
import com.retro99.reader.ui.media.smil.SmilQuickScanner
import com.retro99.reader.ui.model.AudioPositionState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import com.retro99.reader.ui.playback.MediaPlaybackController
import com.retro99.reader.ui.playback.NotificationPermissionHandler
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.r2.shared.util.Url

class AndroidAudioController(
    private val context: Context,
    private val analytics: Analytics,
    private val smilParser: SmilParser,
    private val quickScanner: SmilQuickScanner,
    private val mediaPlaybackController: MediaPlaybackController,
    private val notificationPermissionHandler: NotificationPermissionHandler,
) : AudioController {

    private var currentBookLocation: LocatorState? = null

    private lateinit var mediaOverlayPlayer: MediaOverlayPlayer
    override val currentAudioLocator: StateFlow<LocatorState?> get() = mediaOverlayPlayer.currentLocator

    override val audioPositionState: Flow<AudioPositionState>
        get() = TODO("Not yet implemented")
    override val isPlayingState: Flow<Boolean>
        get() = TODO("Not yet implemented")
    override val playbackState: Flow<PlaybackState>
        get() = TODO("Not yet implemented")
    override val isPlayerReady: Flow<Boolean>
        get() = TODO("Not yet implemented")
    override val showPermissionDeniedDialog: Flow<Boolean>
        get() = TODO("Not yet implemented")
    override val showPermissionRationale: Flow<Boolean>
        get() = TODO("Not yet implemented")

    private lateinit var publication: EpubPublication

    override suspend fun init(
        publication: EpubPublication,
    ) {
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
        mediaOverlayPlayer = MediaOverlayPlayer(
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

        // Get initial chapter href for optimized index building
        val initialChapterHref = publication.initialPosition?.href
            ?: publication.publication.readingOrder.firstOrNull()?.href?.toString()
        val initialChapterUrl = initialChapterHref?.let { Url(it) }
            ?: publication.publication.readingOrder.firstOrNull()?.href?.toString()?.let { Url(it) }
        val initializeStart = nowMillis()
        mediaOverlayPlayer.initialize(initialChapterHref)
        val initializeTime = nowMillis() - initializeStart
        logger.i { "⏱️ player.initialize() (index building) completed in ${initializeTime}ms" }

        val chapterPrepareStart = nowMillis()
        if (initialChapterUrl != null) {
            mediaOverlayPlayer.prepareChapterDuration(initialChapterUrl)
        }
        val chapterPrepareTime = nowMillis() - chapterPrepareStart
        logger.i { "⏱️ prepareChapterDuration() completed in ${chapterPrepareTime}ms" }

        val totalTime = nowMillis() - totalStartTime
        logger.i { "⏱️ MediaOverlay initialization COMPLETE - TOTAL: ${totalTime}ms" }
    }

    override fun playAudio(initialPositionMs: Long?) {
        TODO("Not yet implemented")
    }

    override fun resumeAudio() {
        TODO("Not yet implemented")
    }

    override fun pauseAudio() {
        TODO("Not yet implemented")
    }

    override fun seekToAudioPosition(timestampMs: Long) {
        TODO("Not yet implemented")
    }

    override fun setPlaybackSpeed(speed: Float) {
        TODO("Not yet implemented")
    }

    override fun skipForward() {
        TODO("Not yet implemented")
    }

    override fun skipBackward() {
        TODO("Not yet implemented")
    }

    override fun dismissPermissionDeniedDialog() {
        TODO("Not yet implemented")
    }

    override fun onBookLocationChanged(locator: LocatorState) {
        if (currentBookLocation?.href != locator.href) {
            return
        }
        currentBookLocation = locator
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    private companion object {
        private val logger = Logger.withTag("čič")
    }
}