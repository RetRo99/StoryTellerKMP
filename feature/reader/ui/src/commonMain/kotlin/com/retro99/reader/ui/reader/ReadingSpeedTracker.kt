package com.retro99.reader.ui.reader

import com.retro99.base.nowMillis
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.model.ChapterPageInfo
import com.retro99.reader.ui.model.ChapterReadingTimeInfo
import com.retro99.reader.ui.model.ChapterWordCountInfo
import com.retro99.reader.ui.navigator.BookController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
 * - Tracks reading session start time and page for speed calculation
 * - Calculates dynamic words-per-minute based on actual reading behavior
 * - Exposes a Flow of reading time info that updates on each page turn
 *
 * The dynamic speed calculation works by measuring how many pages the user
 * has read since starting the chapter and how much time has elapsed.
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

    /** Timestamp when the user started reading the current chapter */
    private var chapterReadingStartTimeMs: Long = 0L

    /** The page number when the user started reading the current chapter (1-based) */
    private var chapterReadingStartPage: Int = 1

    /** Dynamically calculated reading speed based on user's actual reading behavior */
    private var calculatedWordsPerMinute: Int? = null

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
            // Reset reading session tracking for new chapter
            chapterReadingStartTimeMs = currentTimeMs
            chapterReadingStartPage = currentPage
            calculatedWordsPerMinute = null
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
     * Calculates the user's actual reading speed based on pages read and time elapsed.
     *
     * @param currentTimeMs Current timestamp in milliseconds
     * @param currentPage Current page number (1-based)
     * @return Calculated words per minute, or previous value/null if not enough data
     */
    private fun calculateDynamicReadingSpeed(
        currentTimeMs: Long,
        currentPage: Int,
    ): Int? {
        val elapsedTimeMs = currentTimeMs - chapterReadingStartTimeMs
        val pagesRead = currentPage - chapterReadingStartPage

        // Need at least 10 seconds and 1 page read to calculate meaningful speed
        // This avoids wild fluctuations from quick scrolling or initial page loads
        if (elapsedTimeMs < MIN_READING_TIME_FOR_SPEED_CALC_MS || pagesRead < MIN_PAGES_FOR_SPEED_CALC) {
            return calculatedWordsPerMinute // Return previous calculated value or null
        }

        val wordsRead = (pagesRead * cachedWordsPerPage).toInt()
        val elapsedMinutes = elapsedTimeMs / 60_000.0

        if (elapsedMinutes <= 0 || wordsRead <= 0) {
            return calculatedWordsPerMinute
        }

        val calculatedWpm = (wordsRead / elapsedMinutes).toInt()

        // Clamp to reasonable bounds (50-1000 WPM) to filter out outliers
        // Very slow might indicate user paused, very fast might indicate scrolling
        return calculatedWpm.coerceIn(MIN_REASONABLE_WPM, MAX_REASONABLE_WPM)
    }

    private companion object {
        /** Minimum time (ms) before calculating dynamic reading speed */
        const val MIN_READING_TIME_FOR_SPEED_CALC_MS = 10_000L // 10 seconds

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

