package com.retro99.reader.ui.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.media.smil.SmilClip
import com.retro99.reader.ui.media.smil.SmilContentProvider
import com.retro99.reader.ui.media.smil.SmilLoadingManager
import com.retro99.reader.ui.media.smil.SmilParser
import com.retro99.reader.ui.media.smil.SmilQuickScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.mediatype.MediaType

/** Interval in milliseconds for position updates during playback */
private const val POSITION_UPDATE_INTERVAL_MS = 100L

/** Conversion factor from seconds to milliseconds */
private const val SECONDS_TO_MS = 1000.0

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
    private val onLocatorChanged: ((Locator) -> Unit)? = null,
) {
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

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    // Custom DataSource.Factory that reads audio from the EPUB container
    private val dataSourceFactory = PublicationDataSource.Factory(publication)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow<Long?>(null)
    val totalDuration: StateFlow<Long?> = _totalDuration.asStateFlow()

    private val _currentLocator = MutableStateFlow<Locator?>(null)
    val currentLocator: StateFlow<Locator?> = _currentLocator.asStateFlow()

    private var positionUpdateJob: Job? = null

    // Current chapter's clips (loaded on-demand)
    private var currentChapterClips: List<MediaOverlayClip> = emptyList()

    // Current audio file being played
    private var currentAudioHref: Url? = null

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
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        // TODO: Move to next chapter or stop
                    }

                    Player.STATE_READY -> {
                        val duration = exoPlayer.duration
                        if (duration > 0) {
                            _totalDuration.value = duration
                        }
                        // Seek to pending initial position if set
                        pendingInitialPositionMs?.let { positionMs ->
                            if (positionMs > 0) {
                                exoPlayer.seekTo(positionMs)
                            }
                            pendingInitialPositionMs = null
                        }
                    }
                }
            }
        })
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

        if (chapterHref != null) {
            prepareChapter(chapterHref, initialFragmentId, initialProgression, initialPositionMs)
        } else {
            // No chapter change - try to seek to explicit position, fragment, or progression
            val positionToSeek = initialPositionMs
                ?: findPositionForFragment(initialFragmentId)
                ?: findPositionForProgression(initialProgression)
            if (positionToSeek != null && positionToSeek > 0) {
                exoPlayer.seekTo(positionToSeek)
            }
        }

        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    /**
     * Finds the audio position in milliseconds for a given text fragment ID.
     *
     * @param fragmentId The fragment ID to find (e.g., "chapter44.xhtml-sentence50")
     * @return The start time in milliseconds, or null if not found
     */
    private fun findPositionForFragment(fragmentId: String?): Long? {
        return fragmentId?.let {
            currentChapterClips.find { clip -> clip.fragmentId == it }
                ?.let { clip -> (clip.startTime * SECONDS_TO_MS).toLong() }
        }
    }

    /**
     * Finds the audio position in milliseconds for a given text progression.
     * Uses the progression to estimate which clip corresponds to that position in the text.
     *
     * @param progression The text progression (0.0 to 1.0) through the chapter
     * @return The start time in milliseconds, or null if clips are empty
     */
    private fun findPositionForProgression(progression: Double?): Long? {
        if (progression == null || progression <= 0.0 || currentChapterClips.isEmpty()) return null

        // Estimate which clip corresponds to this progression
        // If we have 440 clips and progression is 0.09, we want clip ~40
        val clipIndex = (progression * currentChapterClips.size).toInt()
            .coerceIn(0, currentChapterClips.size - 1)

        val clip = currentChapterClips[clipIndex]
        return (clip.startTime * SECONDS_TO_MS).toLong()
    }

    fun pause() {
        exoPlayer.pause()
    }

    /**
     * Resumes playback from the current position without seeking.
     */
    fun resume() {
        exoPlayer.play()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        updateCurrentLocator()
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
            _totalDuration.value = chapterDurationMs
        }

        // Prefetch next chapter in background
        smilLoadingManager.prefetchNextChapter(normalizedHref)
    }

    fun release() {
        stopPositionUpdates()
        exoPlayer.release()
        // Release the SMIL loading manager
        playerScope.launch {
            smilLoadingManager.release()
        }
        // Cancel the player's scope to clean up all coroutines
        playerScope.cancel()
    }

    // Pending initial position to seek to after audio is prepared
    private var pendingInitialPositionMs: Long? = null

    /**
     * Prepares playback for a specific chapter.
     * Uses lazy loading to parse SMIL files on-demand.
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
            prepareChapterAsync(
                chapterHref,
                initialFragmentId,
                initialProgression,
                initialPositionMs
            )
        }
    }

    /**
     * Async implementation of chapter preparation with lazy SMIL loading.
     */
    private suspend fun prepareChapterAsync(
        chapterHref: Url,
        initialFragmentId: String?,
        initialProgression: Double?,
        initialPositionMs: Long?,
    ) {
        val normalizedHref = chapterHref.removeFragment().toString()

        // Load clips for this chapter using lazy loading
        val smilClips = smilLoadingManager.getClipsForChapter(normalizedHref)

        // Convert SmilClip to MediaOverlayClip
        currentChapterClips = convertSmilClipsToMediaOverlayClips(smilClips, chapterHref)

        if (currentChapterClips.isEmpty()) return

        // Calculate total duration from clips (last clip's end time)
        val chapterDurationMs =
            currentChapterClips.maxOfOrNull { (it.endTime * SECONDS_TO_MS).toLong() }
        if (chapterDurationMs != null && chapterDurationMs > 0) {
            _totalDuration.value = chapterDurationMs
        }

        // Get the audio file for this chapter (assuming one audio file per chapter)
        val audioHref = currentChapterClips.firstOrNull()?.audioHref ?: return

        // Determine the position to seek to - prefer explicit position, then fragment, then progression
        val positionToSeek = initialPositionMs
            ?: findPositionForFragment(initialFragmentId)
            ?: findPositionForProgression(initialProgression)

        if (currentAudioHref != audioHref) {
            currentAudioHref = audioHref
            // Store the initial position to seek to after audio is prepared
            pendingInitialPositionMs = positionToSeek
            prepareAudio(audioHref)
        } else if (positionToSeek != null && positionToSeek > 0) {
            // Same audio file, just seek to the position
            exoPlayer.seekTo(positionToSeek)
        }

        // Prefetch next chapter in background
        smilLoadingManager.prefetchNextChapter(normalizedHref)
    }

    /**
     * Prepares the ExoPlayer with the given audio file from the EPUB.
     */
    private fun prepareAudio(audioHref: Url) {
        // Create a MediaItem for the audio file
        // The audio is inside the EPUB, so we need to use the publication's container
        val audioUrl = publication.baseUrl?.resolve(audioHref)?.toString()
            ?: audioHref.toString()

        val mediaItem = MediaItem.fromUri(audioUrl)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = playerScope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition
                updateCurrentLocator()
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    /**
     * Updates the current locator based on the current playback position.
     * This is used to highlight the currently spoken text.
     */
    private fun updateCurrentLocator() {
        val currentTimeSeconds = exoPlayer.currentPosition / SECONDS_TO_MS

        // Find the clip that contains the current time
        val currentClip = currentChapterClips.find { clip ->
            currentTimeSeconds >= clip.startTime && currentTimeSeconds < clip.endTime
        }

        if (currentClip != null && currentClip.fragmentId != null) {
            // Create a locator for the current text fragment
            val locator = Locator(
                href = currentClip.textHref,
                mediaType = MediaType.XHTML,
                locations = Locator.Locations(
                    fragments = listOf(currentClip.fragmentId),
                ),
            )
            // Only update and notify when the locator changes
            if (_currentLocator.value?.locations?.fragments != locator.locations.fragments) {
                _currentLocator.value = locator
                onLocatorChanged?.invoke(locator)
            }
        }
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
