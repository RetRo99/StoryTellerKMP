package com.retro99.reader.ui.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.media.smil.SmilClip
import com.retro99.reader.ui.media.smil.SmilContentProvider
import com.retro99.reader.ui.media.smil.SmilLoadingManager
import com.retro99.reader.ui.media.smil.SmilParser
import com.retro99.reader.ui.media.smil.SmilQuickScanner
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import com.retro99.reader.ui.playback.AudioFocusManager
import com.retro99.reader.ui.playback.ForegroundServiceController
import com.retro99.reader.ui.playback.LocatorTracker
import com.retro99.reader.ui.playback.MediaPlaybackController
import com.retro99.reader.ui.playback.MediaSessionManager
import com.retro99.reader.ui.playback.NotificationPermissionHandler
import com.retro99.reader.ui.playback.PermissionDenialState
import com.retro99.reader.ui.playback.PlaybackStateTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.getOrElse

/** Conversion factor from seconds to milliseconds */
private const val SECONDS_TO_MS = 1000.0

/** Seek increment in milliseconds (10 seconds) */
private const val SEEK_INCREMENT_MS = 10_000L

/**
 * Player for EPUB Media Overlays (SMIL-based text-audio synchronization).
 *
 * This player:
 * 1. Parses SMIL files from the EPUB to get text-audio sync data
 * 2. Uses ExoPlayer to play the audio files
 * 3. Tracks playback position and emits the current Locator for text highlighting
 *
 * Audio files are read directly from the EPUB container using Readium's Publication API
 * and provided to ExoPlayer via ByteArrayDataSource.
 *
 * @param context Android context for ExoPlayer
 * @param publication The Readium Publication containing the EPUB
 */
@OptIn(UnstableApi::class)
class MediaOverlayPlayer(
    private val context: Context,
    private val publication: Publication,
    private val analytics: Analytics,
    private val smilParser: SmilParser,
    private val quickScanner: SmilQuickScanner,
    private val mediaPlaybackController: MediaPlaybackController,
    private val notificationPermissionHandler: NotificationPermissionHandler,
) {
    private val logger = Logger.withTag("MediaOverlayPlayer")

    // Lazy loading manager for SMIL files
    private val smilLoadingManager = SmilLoadingManager(
        smilParser = smilParser,
        quickScanner = quickScanner,
        analytics = analytics,
        ioDispatcher = Dispatchers.IO,
    )

    /**
     * Internal coroutine scope for this player.
     * Uses SupervisorJob so that failure of one child doesn't cancel siblings.
     * Uses Dispatchers.Main for UI-related operations.
     */
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
        .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
        .setHandleAudioBecomingNoisy(true)
        .build()

    // Audio focus manager for handling system audio policy
    private val audioFocusManager = AudioFocusManager(context, exoPlayer)

    // Media session manager for lockscreen/notification/Bluetooth integration
    private val mediaSessionManager = MediaSessionManager(
        context = context,
        player = exoPlayer,
        controller = mediaPlaybackController,
        onUserPausedFromSession = { audioFocusManager.onUserPaused() },
    )

    // Foreground service controller
    private val foregroundServiceController = ForegroundServiceController(context)

    // Locator tracker for position updates and text highlighting
    private val locatorTracker = LocatorTracker(exoPlayer, playerScope)

    // Playback state tracker with callbacks for state changes
    private val playbackStateTracker: PlaybackStateTracker by lazy {
        PlaybackStateTracker(
            player = exoPlayer,
            analytics = analytics,
            onPlaybackEnded = {
                audioFocusManager.abandonFocus()
                foregroundServiceController.stopService()
            },
            onPlayerReady = { _, pendingPosition ->
                pendingPosition?.let { positionMs ->
                    if (positionMs > 0) {
                        exoPlayer.seekTo(positionMs)
                    }
                }
            },
            isPlayingChanged = { isPlaying ->
                if (isPlaying) {
                    locatorTracker.startPositionUpdates()
                } else {
                    locatorTracker.stopPositionUpdates()
                }
            },
            onPlayerError = {
                // Clean up on ExoPlayer error to prevent zombie notification
                audioFocusManager.abandonFocus()
                foregroundServiceController.stopService()
            },
        )
    }

    // Custom DataSource.Factory that reads audio from the EPUB container
    private val dataSourceFactory = PublicationDataSource.Factory(publication)

    // Expose state from trackers
    val isPlaying: StateFlow<Boolean> get() = playbackStateTracker.isPlaying
    val playbackState: StateFlow<PlaybackState> get() = playbackStateTracker.playbackState
    val currentPosition: StateFlow<Long?> get() = locatorTracker.currentPosition
    val totalDuration: StateFlow<Long?> get() = playbackStateTracker.totalDuration
    val currentLocator: StateFlow<LocatorState?> get() = locatorTracker.currentLocator

    // Event to signal that notification permission was denied
    private val _showPermissionDeniedDialog = MutableStateFlow(false)
    val showPermissionDeniedDialog: StateFlow<Boolean> = _showPermissionDeniedDialog.asStateFlow()

    /**
     * Flow that emits true when the permission denial dialog should show a rationale
     * (user can be asked again) vs directing to settings (permanently denied).
     */
    val showPermissionRationale: StateFlow<Boolean> =
        notificationPermissionHandler.denialState.map { state ->
            state is PermissionDenialState.ShowRationale
        }.stateIn(playerScope, SharingStarted.Eagerly, false)

    // Book metadata for media session
    private var bookTitle: String = "Reading Aloud"

    // Current audio file being played
    private var currentAudioHref: Url? = null

    // Mutex to prevent concurrent playInternal() calls from double-taps
    private val playMutex = Mutex()

    // Content provider for reading SMIL files from the publication
    private val contentProvider = object : SmilContentProvider {
        override suspend fun readSmilContent(smilHref: String): String? {
            return withContext(Dispatchers.IO) {
                try {
                    val url = Url(smilHref) ?: return@withContext null
                    val resource = publication.get(url) ?: return@withContext null
                    val bytes = resource.read().getOrElse { return@withContext null }
                    bytes.decodeToString()
                } catch (e: Exception) {
                    analytics.logException(e, "Failed to read SMIL file: $smilHref")
                    null
                }
            }
        }

        override fun getAllSmilHrefs(): List<String> {
            return publication.resources
                .filter { link ->
                    link.mediaType?.toString()?.contains("smil") == true ||
                            link.href.toString().endsWith(".smil")
                }
                .map { it.href.toString() }
        }

        override fun getReadingOrder(): List<String> {
            return publication.readingOrder.map { it.href.toString() }
        }

        override fun resolveSmilPath(smilHref: String, relativePath: String): String {
            val smilUrl = Url(smilHref) ?: return relativePath
            val relativeUrl = Url(relativePath) ?: return relativePath
            return smilUrl.resolve(relativeUrl).toString()
        }
    }

    init {
        // Trigger lazy initialization of playback state tracker
        // This registers the Player.Listener for state tracking
        playbackStateTracker

        // Configure audio attributes for speech content
        audioFocusManager.configurePlayerAudioAttributes()

        // Initialize media session for system integration
        mediaSessionManager.initialize()

        // Set initial book title from publication metadata
        bookTitle = publication.metadata.title ?: "Reading Aloud"
        mediaSessionManager.updateMetadata(bookTitle)
    }

    /**
     * Initializes the player with lazy SMIL loading.
     *
     * This builds a lightweight index of SMIL files without fully parsing them.
     * Full parsing happens on-demand when a chapter is prepared.
     *
     * @param initialChapterHref The initial chapter href to optimize index building for
     */
    suspend fun initialize(initialChapterHref: String? = null) {
        smilLoadingManager.initialize(contentProvider, playerScope)

        // Build initial index for current chapter and nearby chapters
        val chapterHref = initialChapterHref
            ?: publication.readingOrder.firstOrNull()?.href?.toString()
            ?: return

        smilLoadingManager.buildInitialIndex(chapterHref)
    }

    /**
     * Starts or resumes playback for the current chapter.
     *
     * @param chapterHref The href of the current chapter (XHTML file)
     * @param initialFragmentId Optional fragment ID to start from (e.g., "chapter44.xhtml-sentence50")
     * @param initialProgression Optional text progression (0.0 to 1.0) to estimate audio position
     * @param initialPositionMs Optional initial position in milliseconds to seek to before playing
     *                          (used if fragment ID and progression are not provided or not found)
     */
    fun play(
        chapterHref: Url? = null,
        initialFragmentId: String? = null,
        initialProgression: Double? = null,
        initialPositionMs: Long? = null,
    ) {
        playerScope.launch {
            // Use tryLock to prevent concurrent play calls from double-taps
            // If already playing/starting, ignore the second tap
            if (!playMutex.tryLock()) {
                logger.d { "play() ignored - already in progress" }
                return@launch
            }
            try {
                playInternal(chapterHref, initialFragmentId, initialProgression, initialPositionMs)
            } finally {
                playMutex.unlock()
            }
        }
    }

    private suspend fun playInternal(
        chapterHref: Url?,
        initialFragmentId: String?,
        initialProgression: Double?,
        initialPositionMs: Long?,
    ) {
        // Request notification permission before starting foreground service (Android 13+)
        // NOTE: We intentionally do NOT set optimistic state (isPlaying=true, BUFFERING)
        // before permission is granted. This prevents UI flicker where the play button
        // briefly shows "playing" state before reverting if permission is denied.
        val permissionGranted = notificationPermissionHandler.ensurePermission()
        if (!permissionGranted) {
            _showPermissionDeniedDialog.value = true
            return
        }

        // Request audio focus before starting playback
        val focusGranted = audioFocusManager.requestFocus()
        if (!focusGranted) {
            analytics.logException(
                IllegalStateException("Failed to acquire audio focus"),
                "Could not acquire audio focus for playback",
            )
            return
        }

        // Start foreground service for background playback
        val serviceStarted = foregroundServiceController.startService()
        if (!serviceStarted) {
            // App was backgrounded during permission dialog or other system restriction
            audioFocusManager.abandonFocus()
            analytics.logException(
                IllegalStateException("Cannot start foreground service from background"),
                "Foreground service start blocked by system",
            )
            return
        }

        // Set playWhenReady so playback starts automatically when audio is ready
        exoPlayer.playWhenReady = true

        if (chapterHref != null) {
            // prepareChapter will set the media source and call prepare()
            // playback will start automatically because playWhenReady is true
            prepareChapter(chapterHref, initialFragmentId, initialProgression, initialPositionMs)
        } else {
            // No chapter change - try to seek to explicit position, fragment, or progression
            val positionToSeek = initialPositionMs
                ?: locatorTracker.findPositionForFragment(initialFragmentId)
                ?: locatorTracker.findPositionForProgression(initialProgression)
            if (positionToSeek != null && positionToSeek > 0) {
                exoPlayer.seekTo(positionToSeek)
            }
            // Only call play() directly if we're not preparing a new chapter
            // (the chapter is already loaded)
            exoPlayer.play()
        }
    }

    fun pause() {
        // Notify audio focus manager that user manually paused
        // This prevents auto-resume when focus is regained
        audioFocusManager.onUserPaused()
        exoPlayer.pause()
    }

    /**
     * Resumes playback from the current position without seeking.
     */
    fun resume() {
        exoPlayer.play()
    }

    /**
     * Dismisses the permission denied dialog and clears the denial state.
     * This ensures stale denial state doesn't persist across book sessions.
     */
    fun dismissPermissionDeniedDialog() {
        _showPermissionDeniedDialog.value = false
        notificationPermissionHandler.clearDenialState()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        locatorTracker.forceUpdatePosition()
    }

    /**
     * Seeks forward by 10 seconds from the current position.
     * The position is clamped to the duration of the current media.
     */
    fun seekForward() {
        val newPosition = (exoPlayer.currentPosition + SEEK_INCREMENT_MS)
            .coerceAtMost(exoPlayer.duration.coerceAtLeast(0L))
        seekTo(newPosition)
    }

    /**
     * Seeks backward by 10 seconds from the current position.
     * The position is clamped to 0.
     */
    fun seekBackward() {
        val newPosition = (exoPlayer.currentPosition - SEEK_INCREMENT_MS)
            .coerceAtLeast(0L)
        seekTo(newPosition)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }

    /**
     * Prepares the duration for a specific chapter without starting playback.
     * This allows the UI to show the chapter duration before the user presses play.
     *
     * Uses lazy loading to parse SMIL files on-demand.
     *
     * @param chapterHref The href of the chapter to get duration for
     */
    suspend fun prepareChapterDuration(chapterHref: Url) {
        val normalizedHref = chapterHref.removeFragment().toString()

        // Load clips for this chapter using lazy loading
        val smilClips = smilLoadingManager.getClipsForChapter(normalizedHref)

        // Convert SmilClip to MediaOverlayClip
        val chapterClips = convertSmilClipsToMediaOverlayClips(smilClips, chapterHref)

        if (chapterClips.isEmpty()) {
            return
        }

        // Calculate total duration from clips (last clip's end time)
        val chapterDurationMs = chapterClips.maxOfOrNull { (it.endTime * SECONDS_TO_MS).toLong() }
        if (chapterDurationMs != null && chapterDurationMs > 0) {
            playbackStateTracker.setTotalDuration(chapterDurationMs)
            locatorTracker.resetCurrentPosition()
        }

        // Prefetch next chapter in background
        smilLoadingManager.prefetchNextChapter(normalizedHref)
    }

    fun release() {
        // CRITICAL: Cancel the scope FIRST to stop any in-flight coroutines
        // that might be using exoPlayer. If we release exoPlayer while
        // playInternal() or prepareChapterAsync() is running, ExoPlayer throws
        // IllegalStateException on any method call after release().
        playerScope.cancel()

        // Release trackers
        locatorTracker.release()
        playbackStateTracker.release()

        // Abandon audio focus
        audioFocusManager.abandonFocus()

        // Release media session
        mediaSessionManager.release()

        // Stop foreground service
        foregroundServiceController.stopService()

        // Now safe to release ExoPlayer - no coroutines are using it
        exoPlayer.release()
        smilLoadingManager.release()
    }

    /**
     * Prepares playback for a specific chapter.
     * Uses lazy loading to parse SMIL files on-demand.
     *
     * If preparation fails (exception, no clips, no audio), the foreground service
     * is stopped and playback state is reset to prevent zombie notifications.
     *
     * @param chapterHref The href of the chapter to prepare
     * @param initialFragmentId Optional fragment ID to start from
     * @param initialProgression Optional text progression (0.0 to 1.0) to estimate audio position
     * @param initialPositionMs Optional initial position to seek to after audio is ready
     *                          (used if fragment ID and progression are not provided or not found)
     */
    private fun prepareChapter(
        chapterHref: Url,
        initialFragmentId: String? = null,
        initialProgression: Double? = null,
        initialPositionMs: Long? = null,
    ) {
        playerScope.launch {
            try {
                val success = prepareChapterAsync(
                    chapterHref,
                    initialFragmentId,
                    initialProgression,
                    initialPositionMs,
                )
                if (!success) {
                    handlePreparationFailure("Chapter preparation returned no playable content")
                }
            } catch (e: Exception) {
                analytics.logException(e, "Failed to prepare chapter: $chapterHref")
                handlePreparationFailure("Exception during chapter preparation: ${e.message}")
            }
        }
    }

    /**
     * Handles preparation failure by stopping the foreground service and resetting state.
     * This prevents zombie notifications when SMIL parsing fails, audio is missing, etc.
     */
    private fun handlePreparationFailure(reason: String) {
        logger.e { "Preparation failed: $reason" }
        playbackStateTracker.setPlayingState(false)
        playbackStateTracker.setPlaybackState(PlaybackState.ERROR)
        audioFocusManager.abandonFocus()
        foregroundServiceController.stopService()
    }

    /**
     * Async implementation of chapter preparation with lazy SMIL loading.
     *
     * @return true if audio was successfully prepared, false if no playable content found
     */
    private suspend fun prepareChapterAsync(
        chapterHref: Url,
        initialFragmentId: String?,
        initialProgression: Double?,
        initialPositionMs: Long?,
    ): Boolean {
        logger.d { "prepareChapterAsync() - chapterHref=$chapterHref" }
        val normalizedHref = chapterHref.removeFragment().toString()

        // Load clips for this chapter using lazy loading
        val smilClips = smilLoadingManager.getClipsForChapter(normalizedHref)
        logger.d { "SMIL clips loaded: ${smilClips.size} clips" }

        // Convert SmilClip to MediaOverlayClip and update locator tracker
        val chapterClips = convertSmilClipsToMediaOverlayClips(smilClips, chapterHref)
        locatorTracker.setChapterClips(chapterClips)
        logger.d { "Converted to MediaOverlayClips: ${chapterClips.size} clips" }

        if (chapterClips.isEmpty()) {
            logger.w { "No clips found for chapter" }
            return false
        }

        // Calculate total duration from clips (last clip's end time)
        val chapterDurationMs = chapterClips.maxOfOrNull { (it.endTime * SECONDS_TO_MS).toLong() }
        logger.d { "Chapter duration: ${chapterDurationMs}ms" }
        if (chapterDurationMs != null && chapterDurationMs > 0) {
            playbackStateTracker.setTotalDuration(chapterDurationMs)
        }

        // Get the audio file for this chapter (assuming one audio file per chapter)
        val audioHref = chapterClips.firstOrNull()?.audioHref
        logger.d { "Audio href: $audioHref" }
        if (audioHref == null) {
            logger.w { "No audio href found" }
            return false
        }

        // Determine the position to seek to - prefer explicit position, then fragment, then progression
        val positionToSeek = initialPositionMs
            ?: locatorTracker.findPositionForFragment(initialFragmentId)
            ?: locatorTracker.findPositionForProgression(initialProgression)
        logger.d { "Position to seek: $positionToSeek" }

        if (currentAudioHref != audioHref) {
            logger.d { "New audio file, preparing: $audioHref (was: $currentAudioHref)" }
            currentAudioHref = audioHref
            // Store the initial position to seek to after audio is prepared
            playbackStateTracker.setPendingSeekPosition(positionToSeek)
            prepareAudio(audioHref)
        } else if (positionToSeek != null && positionToSeek > 0) {
            logger.d { "Same audio file, seeking to $positionToSeek" }
            // Same audio file, just seek to the position
            exoPlayer.seekTo(positionToSeek)
        }

        // Prefetch next chapter in background
        smilLoadingManager.prefetchNextChapter(normalizedHref)
        return true
    }

    /**
     * Prepares the ExoPlayer with the given audio file from the EPUB.
     *
     * Sets the media metadata on the MediaItem itself (not just playlistMetadata)
     * because Media3's notification controller prefers MediaItem.mediaMetadata.
     */
    private fun prepareAudio(audioHref: Url) {
        // Create a MediaItem for the audio file
        // The audio is inside the EPUB, so we need to use the publication's container
        val audioUrl = publication.baseUrl?.resolve(audioHref)?.toString()
            ?: audioHref.toString()
        logger.d { "prepareAudio() - audioUrl=$audioUrl" }

        // Build MediaItem with metadata for proper notification/lockscreen display
        val mediaItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMediaMetadata(mediaSessionManager.buildCurrentMetadata())
            .build()

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        // Use resetPosition=true to start from beginning (we'll seek after prepare)
        exoPlayer.setMediaSource(mediaSource, /* resetPosition= */ true)
        exoPlayer.prepare()
    }

    /**
     * Converts SmilClip (from shared parser) to MediaOverlayClip (Android-specific).
     *
     * SmilClip contains raw string references, while MediaOverlayClip uses Readium Url objects.
     * This method resolves the relative paths and extracts fragment IDs.
     *
     * @param smilClips The raw clips from the shared parser
     * @param chapterHref The chapter href for context (used to resolve relative paths)
     * @return List of MediaOverlayClip with resolved URLs
     */
    private fun convertSmilClipsToMediaOverlayClips(
        smilClips: List<SmilClip>,
        chapterHref: Url,
    ): List<MediaOverlayClip> {
        return smilClips.mapNotNull { raw ->
            try {
                val textUrl = Url(raw.textSrc) ?: return@mapNotNull null
                // Resolve relative to chapter href's directory
                val resolvedTextUrl = chapterHref.resolve(textUrl)
                val fragmentId = resolvedTextUrl.fragment

                val audioUrl = Url(raw.audioSrc) ?: return@mapNotNull null
                val resolvedAudioUrl = chapterHref.resolve(audioUrl)

                MediaOverlayClip(
                    textHref = resolvedTextUrl.removeFragment(),
                    fragmentId = fragmentId,
                    audioHref = resolvedAudioUrl.removeFragment(),
                    startTime = raw.clipBegin,
                    endTime = raw.clipEnd,
                )
            } catch (e: Exception) {
                analytics.logException(e, "Failed to convert clip")
                null
            }
        }
    }
}
