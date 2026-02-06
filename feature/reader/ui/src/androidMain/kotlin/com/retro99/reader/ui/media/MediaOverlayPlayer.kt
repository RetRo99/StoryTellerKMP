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
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

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
    private val onLocatorChanged: ((Locator) -> Unit)? = null,
) {
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

    // All clips from all SMIL files, sorted by audio file and start time
    private var allClips: List<MediaOverlayClip> = emptyList()

    // Current chapter's clips
    private var currentChapterClips: List<MediaOverlayClip> = emptyList()

    // Current audio file being played
    private var currentAudioHref: Url? = null

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
     * Initializes the player by parsing all SMIL files from the publication.
     */
    suspend fun initialize() {
        allClips = parseAllSmilFiles()
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
     * @param chapterHref The href of the chapter to get duration for
     */
    fun prepareChapterDuration(chapterHref: Url) {
        // Find clips for this chapter
        val chapterClips = allClips.filter { clip ->
            clip.textHref.removeFragment() == chapterHref.removeFragment()
        }

        if (chapterClips.isEmpty()) {
            return
        }

        // Calculate total duration from clips (last clip's end time)
        val chapterDurationMs = chapterClips.maxOfOrNull { (it.endTime * SECONDS_TO_MS).toLong() }
        if (chapterDurationMs != null && chapterDurationMs > 0) {
            _totalDuration.value = chapterDurationMs
        }
    }

    fun release() {
        stopPositionUpdates()
        exoPlayer.release()
        // Cancel the player's scope to clean up all coroutines
        playerScope.cancel()
    }

    // Pending initial position to seek to after audio is prepared
    private var pendingInitialPositionMs: Long? = null

    /**
     * Prepares playback for a specific chapter.
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
        // Find clips for this chapter
        currentChapterClips = allClips.filter { clip ->
            clip.textHref.removeFragment() == chapterHref.removeFragment()
        }

        if (currentChapterClips.isEmpty()) {
            return
        }

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
                mediaType = org.readium.r2.shared.util.mediatype.MediaType.XHTML,
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
     * Parses all SMIL files from the publication to extract media overlay clips.
     */
    private suspend fun parseAllSmilFiles(): List<MediaOverlayClip> {
        val clips = mutableListOf<MediaOverlayClip>()

        // Find all SMIL resources in the publication
        val smilResources = publication.resources.filter { link ->
            link.mediaType?.toString()?.contains("smil") == true ||
                    link.href.toString().endsWith(".smil")
        }



        for (smilLink in smilResources) {
            try {
                val smilUrl = smilLink.url()
                val smilClips = parseSmilFile(smilUrl)
                clips.addAll(smilClips)
            } catch (e: Exception) {
                analytics.logException(e, "Failed to parse SMIL file: ${smilLink.url()}")
            }
        }

        return clips.sortedWith(compareBy({ it.audioHref.toString() }, { it.startTime }))
    }

    /**
     * Parses a single SMIL file and returns the media overlay clips.
     *
     * SMIL structure for EPUB Media Overlays:
     * ```xml
     * <smil>
     *   <body>
     *     <seq epub:textref="chapter1.xhtml">
     *       <par>
     *         <text src="chapter1.xhtml#s1"/>
     *         <audio src="audio/chapter1.mp3" clipBegin="0s" clipEnd="2.5s"/>
     *       </par>
     *       ...
     *     </seq>
     *   </body>
     * </smil>
     * ```
     */
    private suspend fun parseSmilFile(smilHref: Url): List<MediaOverlayClip> =
        withContext(Dispatchers.IO) {
            val clips = mutableListOf<MediaOverlayClip>()

            try {
                // Get the SMIL file content from the publication
                val resource = publication.get(smilHref)
                if (resource == null) {
                    return@withContext emptyList()
                }
                val bytes = resource.read().getOrElse {
                    return@withContext emptyList()
                }
                val content = bytes.decodeToString()

                // Parse the XML
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val parser = factory.newPullParser()
                parser.setInput(content.reader())

                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "par") {
                        parseParElement(parser, smilHref)?.let { clips.add(it) }
                    }
                    eventType = parser.next()
                }

            } catch (e: Exception) {
                analytics.logException(e, "Error parsing SMIL file: $smilHref")
            }

            clips
        }

    /**
     * Parses a <par> element containing <text> and <audio> children.
     */
    private fun parseParElement(parser: XmlPullParser, smilHref: Url): MediaOverlayClip? {
        var textSrc: String? = null
        var audioSrc: String? = null
        var clipBegin: Double? = null
        var clipEnd: Double? = null

        val depth = parser.depth
        var eventType = parser.next()

        while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "text" -> {
                        textSrc = parser.getAttributeValue(null, "src")
                    }

                    "audio" -> {
                        audioSrc = parser.getAttributeValue(null, "src")
                        clipBegin = parseClockValue(
                            parser.getAttributeValue(null, "clipBegin"),
                        )
                        clipEnd = parseClockValue(parser.getAttributeValue(null, "clipEnd"))
                    }
                }
            }
            eventType = parser.next()
        }

        if (textSrc == null || audioSrc == null) {
            return null
        }

        // Parse the text reference to get href and fragment
        val textUrl = Url(textSrc) ?: return null
        val resolvedTextUrl = smilHref.resolve(textUrl)
        val fragmentId = resolvedTextUrl.fragment

        // Parse the audio reference
        val audioUrl = Url(audioSrc) ?: return null
        val resolvedAudioUrl = smilHref.resolve(audioUrl)

        return MediaOverlayClip(
            textHref = resolvedTextUrl.removeFragment(),
            fragmentId = fragmentId,
            audioHref = resolvedAudioUrl.removeFragment(),
            startTime = clipBegin ?: 0.0,
            endTime = clipEnd ?: 0.0,
        )
    }

    /**
     * Parses SMIL clock values like "0s", "2.5s", "00:01:30", "1.5h", "90min", "500ms".
     */
    private fun parseClockValue(value: String?): Double? {
        if (value == null) return null
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null

        return try {
            when {
                ":" in trimmed -> parseColonClockValue(trimmed)
                else -> parseMetricClockValue(trimmed)
            }
        } catch (e: Exception) {
            analytics.logException(e, "Error parsing clock value: $value")
            null
        }
    }

    /**
     * Parses clock values in HH:MM:SS or MM:SS format.
     */
    private fun parseColonClockValue(value: String): Double {
        val parts = value.split(":").map { it.toDouble() }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1] // MM:SS
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2] // HH:MM:SS
            else -> parts.last()
        }
    }

    /**
     * Parses clock values with metric suffixes (h, min, s, ms).
     */
    private fun parseMetricClockValue(value: String): Double {
        val metricStart = value.indexOfFirst { it.isLetter() }
        return if (metricStart == -1) {
            value.toDouble()
        } else {
            val count = value.take(metricStart).toDouble()
            val metric = value.substring(metricStart)
            when (metric) {
                "h" -> count * 3600
                "min" -> count * 60
                "s" -> count
                "ms" -> count / 1000
                else -> count
            }
        }
    }
}
