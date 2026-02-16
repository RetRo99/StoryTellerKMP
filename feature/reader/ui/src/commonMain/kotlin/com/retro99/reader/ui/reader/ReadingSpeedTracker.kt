package com.retro99.reader.ui.reader

import com.retro99.base.nowMillis
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.model.ChapterPageInfo
import com.retro99.reader.ui.model.ChapterReadingTimeInfo
import com.retro99.reader.ui.model.ChapterWordCountInfo
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.reader.ReadingSpeedTracker.Companion.MAX_PAGE_DWELL_MS
import com.retro99.reader.ui.reader.ReadingSpeedTracker.Companion.MIN_PAGE_DWELL_MS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/**
 * Tracks the user's reading speed dynamically based on actual page turns.
 *
 * This class encapsulates all reading time and speed calculation logic:
 * - Observes book location changes and settings automatically
 * - Caches word count per chapter (fetched once when chapter changes)
 * - Tracks reading session with idle detection for accurate speed calculation
 * - Calculates dynamic words-per-minute based on actual reading behavior
 * - Exposes a Flow of reading time info that updates on each page turn
 *
 * The dynamic speed calculation works by measuring active reading time only.
 * It detects idle periods (user pausing, app backgrounded, screen off) and
 * excludes them from the calculation to avoid artificially low WPM values.
 *
 * Note: Reading time is only calculated for regular ebooks, not ReadAloud books
 * (books with media overlays), as those have audio-based progress tracking.
 */
@Scope(ReaderScope::class)
@Scoped
class ReadingSpeedTracker(
    private val bookController: BookController,
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
) {

    /** Cached word count for the current chapter */
    private var cachedChapterWordCount: ChapterWordCountInfo? = null

    /** The href of the chapter for which we have cached data */
    private var cachedChapterHref: String? = null

    /** Cached words per page for the current chapter */
    private var cachedWordsPerPage: Double = 0.0

    /** Accumulated active reading time in milliseconds (excludes idle periods) */
    private var activeReadingTimeMs: Long = 0L

    /** Timestamp of the last page turn (used to detect idle periods) */
    private var lastPageTurnTimeMs: Long = 0L

    /** The page number at the last recorded page turn */
    private var lastRecordedPage: Int = 0

    /** Total pages read during active reading (excludes pages after idle) */
    private var totalPagesRead: Int = 0

    /** Dynamically calculated reading speed based on user's actual reading behavior */
    private var calculatedWordsPerMinute: Int? = null

    /**
     * Established reading speed that persists across chapter changes.
     * This is updated when we have a confident measurement and is used as the
     * starting point when entering a new chapter.
     */
    private var establishedWordsPerMinute: Int? = null

    /** Emits the established (confident) reading speed for persistence. */
    private val _establishedReadingSpeedWpm = MutableStateFlow<Int?>(null)
    val establishedReadingSpeedWpm: StateFlow<Int?> = _establishedReadingSpeedWpm.asStateFlow()

    /**
     * Flow of reading time information that updates on each page turn.
     *
     * Emits null when:
     * - Reading time display is disabled in settings
     * - This is a ReadAloud book (has media overlays)
     * - Calculation is not possible (missing data)
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val readingTimeInfo: Flow<ChapterReadingTimeInfo?> = getReaderSettingsUseCase()
        .map { settings ->
            ReadingTimeSettings(
                showReadingTime = settings.showReadingTime,
                readingSpeedWpm = settings.readingSpeedWpm,
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { settings ->
            if (!settings.showReadingTime || bookController.hasMediaOverlays) {
                // Don't calculate for ReadAloud books or when disabled
                flowOf(null)
            } else {
                bookController.currentLocator.map { locator ->
                    calculateReadingTime(
                        chapterHref = locator.href,
                        progression = locator.progression,
                        fallbackWpm = settings.readingSpeedWpm,
                    )
                }
            }
        }

    /**
     * Calculates reading time for the current position.
     */
    private suspend fun calculateReadingTime(
        chapterHref: String,
        progression: Double?,
        fallbackWpm: Int,
    ): ChapterReadingTimeInfo? {
        val currentTimeMs = nowMillis()
        val chapterPageInfo = bookController.getChapterPageInfo()
        val currentPage = chapterPageInfo?.currentPage ?: 1
        val totalPages = chapterPageInfo?.totalPages ?: 1

        // Fetch word count and reset tracking if chapter changed
        if (chapterHref != cachedChapterHref) {
            cachedChapterWordCount = bookController.getChapterWordCount()
            cachedChapterHref = chapterHref
            // Reset all reading session tracking for new chapter
            resetReadingSession(currentTimeMs, currentPage)
            // Calculate words per page for this chapter
            val totalWords = cachedChapterWordCount?.totalWords ?: 0
            cachedWordsPerPage = if (totalPages > 0) totalWords.toDouble() / totalPages else 0.0
        }

        // Calculate dynamic reading speed based on actual page turns
        if (chapterPageInfo != null && cachedWordsPerPage > 0) {
            calculatedWordsPerMinute = calculateDynamicReadingSpeed(
                currentTimeMs = currentTimeMs,
                currentPage = currentPage,
            )
        }

        return buildReadingTimeInfo(
            chapterPageInfo = chapterPageInfo,
            chapterProgression = progression,
            wordsPerMinute = calculatedWordsPerMinute ?: fallbackWpm,
        )
    }

    /**
     * Resets chapter-specific reading session tracking state.
     *
     * Preserves the established reading speed across chapter changes so the user
     * doesn't fall back to the default WPM every time they switch chapters.
     * The established speed will be blended with new measurements from the current chapter.
     */
    private fun resetReadingSession(currentTimeMs: Long, currentPage: Int) {
        // Save current calculated WPM as established before resetting
        calculatedWordsPerMinute?.let { currentWpm ->
            establishedWordsPerMinute = currentWpm
        }

        activeReadingTimeMs = 0L
        lastPageTurnTimeMs = currentTimeMs
        lastRecordedPage = currentPage
        totalPagesRead = 0
        // Start new chapter with established speed (will be blended with new measurements)
        calculatedWordsPerMinute = establishedWordsPerMinute
    }

    /**
     * Builds the reading time info based on word count and reading progress.
     * Uses page-based calculation when available for more precise estimation.
     */
    private fun buildReadingTimeInfo(
        chapterPageInfo: ChapterPageInfo?,
        chapterProgression: Double?,
        wordsPerMinute: Int,
    ): ChapterReadingTimeInfo? {
        val wordCountInfo = cachedChapterWordCount
        if (wordCountInfo == null || wordsPerMinute <= 0) {
            return null
        }

        val totalWords = wordCountInfo.totalWords

        // Use page-based calculation if available (more precise)
        val remainingWords = if (chapterPageInfo != null && cachedWordsPerPage > 0) {
            val remainingPages = (chapterPageInfo.totalPages - chapterPageInfo.currentPage)
                .coerceAtLeast(0)
            (remainingPages * cachedWordsPerPage).toInt()
        } else if (chapterProgression != null) {
            // Fallback to progression-based calculation
            val remainingFraction = (1.0 - chapterProgression).coerceIn(0.0, 1.0)
            (totalWords * remainingFraction).toInt()
        } else {
            return null
        }

        val remainingMinutes = (remainingWords.toDouble() / wordsPerMinute).toInt()

        return ChapterReadingTimeInfo(
            remainingMinutes = remainingMinutes,
            remainingWords = remainingWords,
            totalWords = totalWords,
        )
    }

    /**
     * Calculates the user's actual reading speed based on pages read and active reading time.
     *
     * This method tracks only "active" reading time by enforcing a dwell-time window.
     * A page turn only counts toward reading speed if the time spent on the page is
     * between [MIN_PAGE_DWELL_MS] and [MAX_PAGE_DWELL_MS].
     * - Below the minimum: treated as fast navigation/skimming
     * - Above the maximum: treated as idle (app backgrounded, stepped away, etc.)
     *
     * Backward navigation (pagesMoved < 0) is also excluded from active reading time,
     * as the user is re-reading or navigating rather than progressing through new content.
     *
     * @param currentTimeMs Current timestamp in milliseconds
     * @param currentPage Current page number (1-based)
     * @return Calculated words per minute, or previous value/null if not enough data
     */
    private fun calculateDynamicReadingSpeed(
        currentTimeMs: Long,
        currentPage: Int,
    ): Int? {
        val timeSinceLastPageTurn = currentTimeMs - lastPageTurnTimeMs
        val pagesMoved = currentPage - lastRecordedPage

        // Only process if the page actually changed
        if (pagesMoved != 0) {
            val isWithinDwellWindow =
                timeSinceLastPageTurn in MIN_PAGE_DWELL_MS..MAX_PAGE_DWELL_MS

            if (!isWithinDwellWindow || pagesMoved < 0) {
                // User was idle, navigated too fast, or went backward - don't count this time.
                // Just update the tracking state without adding to active time.
                // Backward navigation is excluded because:
                // 1. The user is re-reading content, not progressing
                // 2. We can't accurately measure reading speed during navigation
                // 3. Including this time would artificially skew the calculated WPM
                lastPageTurnTimeMs = currentTimeMs
                lastRecordedPage = currentPage
            } else {
                // Active forward reading - count this time and pages
                activeReadingTimeMs += timeSinceLastPageTurn
                totalPagesRead += pagesMoved
                lastPageTurnTimeMs = currentTimeMs
                lastRecordedPage = currentPage
            }
        }

        // Need minimum active reading time and pages to calculate meaningful speed
        if (activeReadingTimeMs < MIN_READING_TIME_FOR_SPEED_CALC_MS ||
            totalPagesRead < MIN_PAGES_FOR_SPEED_CALC
        ) {
            return calculatedWordsPerMinute // Return previous calculated value or null
        }

        val wordsRead = (totalPagesRead * cachedWordsPerPage).toInt()
        val activeMinutes = activeReadingTimeMs / 60_000.0

        if (activeMinutes <= 0 || wordsRead <= 0) {
            return calculatedWordsPerMinute
        }

        val rawWpm = (wordsRead / activeMinutes).toInt()

        // Blend with established reading speed if available
        // This provides smoothing and prevents jarring changes between chapters
        val blendedWpm = establishedWordsPerMinute?.let { established ->
            // Weight new measurement more as we accumulate more reading time in this chapter
            // After ~1 minute of reading, new measurement has ~50% weight
            val newMeasurementWeight = (activeMinutes / (activeMinutes + 1.0)).coerceIn(0.0, 0.8)
            val establishedWeight = 1.0 - newMeasurementWeight
            (established * establishedWeight + rawWpm * newMeasurementWeight).toInt()
        } ?: rawWpm

        // Clamp to reasonable bounds (50-1000 WPM) to filter out outliers
        val finalWpm = blendedWpm.coerceIn(MIN_REASONABLE_WPM, MAX_REASONABLE_WPM)

        // Update established speed when we have confident measurement (enough reading time)
        if (activeReadingTimeMs >= CONFIDENT_READING_TIME_MS) {
            establishedWordsPerMinute = finalWpm
            if (_establishedReadingSpeedWpm.value != finalWpm) {
                _establishedReadingSpeedWpm.value = finalWpm
            }
        }

        return finalWpm
    }

    private companion object {
        /**
         * Minimum time in milliseconds a user must spend on a page for it to count
         * toward reading speed. Shorter gaps are treated as fast navigation/skimming.
         */
        const val MIN_PAGE_DWELL_MS = 10 * 1000L // 10 seconds

        /**
         * Maximum time in milliseconds a user can spend on a page for it to count
         * toward reading speed. Longer gaps are treated as idle time.
         */
        const val MAX_PAGE_DWELL_MS = 3 * 60 * 1000L // 3 minutes

        /** Minimum active reading time (ms) before calculating dynamic reading speed */
        const val MIN_READING_TIME_FOR_SPEED_CALC_MS = 10_000L // 10 seconds

        /**
         * Reading time (ms) required before we consider the measurement confident enough
         * to update the established reading speed. This prevents short bursts of reading
         * from overwriting a well-established reading speed.
         */
        const val CONFIDENT_READING_TIME_MS = 30_000L // 30 seconds

        /** Minimum pages read before calculating dynamic reading speed */
        const val MIN_PAGES_FOR_SPEED_CALC = 1

        /** Minimum reasonable reading speed (WPM) */
        const val MIN_REASONABLE_WPM = 50

        /** Maximum reasonable reading speed (WPM) */
        const val MAX_REASONABLE_WPM = 1000
    }
}

/**
 * Settings relevant to reading time calculation.
 */
private data class ReadingTimeSettings(
    val showReadingTime: Boolean,
    val readingSpeedWpm: Int,
)

