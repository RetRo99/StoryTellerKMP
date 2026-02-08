package com.retro99.reader.ui.playback

import androidx.media3.exoplayer.ExoPlayer
import com.retro99.reader.ui.media.MediaOverlayClip
import com.retro99.reader.ui.model.LocatorState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.mediatype.MediaType

/** Interval in milliseconds for position updates during playback */
private const val POSITION_UPDATE_INTERVAL_MS = 100L

/** Conversion factor from seconds to milliseconds */
private const val SECONDS_TO_MS = 1000.0

/**
 * Tracks the current playback position and maps it to text locators.
 *
 * This class is responsible for:
 * - Periodically updating the current position during playback
 * - Finding the current clip based on position
 * - Emitting locators for text highlighting
 *
 * @param player The ExoPlayer instance to track position from
 * @param scope CoroutineScope for position update job
 * @param onLocatorChanged Optional callback when the locator changes
 */
class LocatorTracker(
    private val player: ExoPlayer,
    private val scope: CoroutineScope,
) {
    private val _currentPosition = MutableStateFlow<Long?>(null)
    val currentPosition: StateFlow<Long?> = _currentPosition

    private val _currentLocator = MutableStateFlow<Locator?>(null)
    val currentLocator: StateFlow<LocatorState?> = _currentLocator.asStateFlow()
        .map { locator ->
            if (locator == null) return@map null
            LocatorState(
                href = locator.href.toString(),
                type = locator.mediaType.toString(),
                title = locator.title,
                progression = locator.locations.progression,
                position = locator.locations.position,
                totalProgression = locator.locations.totalProgression,
                fragments = locator.locations.fragments,
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private var positionUpdateJob: Job? = null

    // Current chapter's clips (set externally when chapter changes)
    private var currentChapterClips: List<MediaOverlayClip> = emptyList()

    /**
     * Updates the clips for the current chapter.
     * Called when a new chapter is prepared.
     */
    fun setChapterClips(clips: List<MediaOverlayClip>) {
        currentChapterClips = clips
    }

    /**
     * Gets the current chapter clips.
     */
    fun getChapterClips(): List<MediaOverlayClip> = currentChapterClips

    /**
     * Starts periodic position updates.
     * Should be called when playback starts.
     */
    fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                _currentPosition.value = player.currentPosition
                updateCurrentLocator()
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Stops periodic position updates.
     * Should be called when playback pauses or stops.
     */
    fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    /**
     * Finds the audio position in milliseconds for a given text fragment ID.
     *
     * @param fragmentId The fragment ID to find (e.g., "chapter44.xhtml-sentence50")
     * @return The start time in milliseconds, or null if not found
     */
    fun findPositionForFragment(fragmentId: String?): Long? {
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
    fun findPositionForProgression(progression: Double?): Long? {
        if (progression == null || progression <= 0.0 || currentChapterClips.isEmpty()) return null

        // Estimate which clip corresponds to this progression
        val clipIndex = (progression * currentChapterClips.size).toInt()
            .coerceIn(0, currentChapterClips.size - 1)

        val clip = currentChapterClips[clipIndex]
        return (clip.startTime * SECONDS_TO_MS).toLong()
    }

    /**
     * Updates the current locator based on the current playback position.
     * This is used to highlight the currently spoken text.
     */
    private fun updateCurrentLocator() {
        val currentTimeSeconds = player.currentPosition / SECONDS_TO_MS

        // Find the clip that contains the current time using binary search
        // This is O(log n) instead of O(n), important since this runs every 100ms
        val currentClip = findClipAtTime(currentTimeSeconds)

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
            }
        }
    }

    /**
     * Finds the clip containing the given time using binary search.
     *
     * Clips are assumed to be sorted by startTime (which they are from SMIL parsing).
     * Uses binary search to find the clip where startTime <= time < endTime.
     *
     * @param timeSeconds The time in seconds to search for
     * @return The clip containing the time, or null if not found
     */
    private fun findClipAtTime(timeSeconds: Double): MediaOverlayClip? {
        if (currentChapterClips.isEmpty()) return null

        var low = 0
        var high = currentChapterClips.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val clip = currentChapterClips[mid]

            when {
                timeSeconds < clip.startTime -> high = mid - 1
                timeSeconds >= clip.endTime -> low = mid + 1
                else -> return clip // timeSeconds is within [startTime, endTime)
            }
        }

        return null
    }

    /**
     * Forces an update of the current position and locator.
     * Called after seeking to ensure UI reflects the new position.
     */
    fun forceUpdatePosition() {
        _currentPosition.value = player.currentPosition
        updateCurrentLocator()
    }

    /**
     * Releases resources. Call when the player is released.
     */
    fun release() {
        stopPositionUpdates()
        currentChapterClips = emptyList()
    }
}

