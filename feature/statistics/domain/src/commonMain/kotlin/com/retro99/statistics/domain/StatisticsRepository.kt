package com.retro99.statistics.domain

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.domain.model.BookType
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel
import com.retro99.statistics.domain.model.DailyReadingTimeDomainModel
import com.retro99.statistics.domain.model.ReadingSessionDomainModel
import com.retro99.statistics.domain.model.ReadingStatisticsDomainModel
import com.retro99.statistics.domain.model.ReadingStreakDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for reading statistics operations.
 */
interface StatisticsRepository {

    /**
     * Saves a new reading session.
     */
    suspend fun saveReadingSession(session: ReadingSessionDomainModel): CompletableResult

    /**
     * Gets all reading sessions.
     */
    suspend fun getAllSessions(): AppResult<List<ReadingSessionDomainModel>>

    /**
     * Gets reading sessions for a specific book.
     */
    suspend fun getSessionsByBook(bookUuid: String): AppResult<List<ReadingSessionDomainModel>>

    /**
     * Gets aggregated reading statistics.
     */
    fun getReadingStatistics(): Flow<AppResult<ReadingStatisticsDomainModel>>

    /**
     * Gets total reading time in milliseconds.
     */
    suspend fun getTotalReadingTimeMs(): AppResult<Long>

    /**
     * Gets reading time for today in milliseconds.
     */
    suspend fun getTodayReadingTimeMs(): AppResult<Long>

    /**
     * Gets reading time for this week in milliseconds.
     */
    suspend fun getWeekReadingTimeMs(): AppResult<Long>

    /**
     * Gets reading time for this month in milliseconds.
     */
    suspend fun getMonthReadingTimeMs(): AppResult<Long>

    /**
     * Gets daily reading time for the last N days.
     */
    suspend fun getDailyReadingTime(days: Int): AppResult<List<DailyReadingTimeDomainModel>>

    /**
     * Gets the most read books.
     */
    suspend fun getMostReadBooks(limit: Int): AppResult<List<BookReadingStatsDomainModel>>

    /**
     * Gets the most read books within a specific date range.
     */
    suspend fun getMostReadBooksInDateRange(
        startTime: Long,
        endTime: Long,
        limit: Int,
    ): AppResult<List<BookReadingStatsDomainModel>>

    /**
     * Gets reading time grouped by book type.
     */
    suspend fun getReadingTimeByType(): AppResult<Map<BookType, Long>>

    /**
     * Gets reading streak information.
     */
    suspend fun getReadingStreak(): AppResult<ReadingStreakDomainModel>

    /**
     * Deletes all reading sessions.
     */
    suspend fun clearAllSessions(): CompletableResult
}

