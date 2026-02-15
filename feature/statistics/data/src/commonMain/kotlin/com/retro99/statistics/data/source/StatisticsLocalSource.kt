package com.retro99.statistics.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel
import com.retro99.statistics.domain.model.DailyReadingTimeDomainModel
import com.retro99.statistics.domain.model.ReadingSessionDomainModel

/**
 * Local data source interface for reading statistics.
 */
interface StatisticsLocalSource {

    suspend fun insertSession(session: ReadingSessionDomainModel): CompletableResult

    suspend fun getAllSessions(): AppResult<List<ReadingSessionDomainModel>>

    suspend fun getSessionsByBookUuid(bookUuid: String): AppResult<List<ReadingSessionDomainModel>>

    suspend fun getSessionsInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<List<ReadingSessionDomainModel>>

    suspend fun getTotalReadingTimeMs(): AppResult<Long>

    suspend fun getTotalReadingTimeMsInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<Long>

    suspend fun getSessionCountInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<Long>

    suspend fun getDistinctBooksReadInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<Long>

    suspend fun getDailyReadingTime(
        sinceTimestamp: Long,
    ): AppResult<List<DailyReadingTimeDomainModel>>

    suspend fun getReadingTimeByBookType(
        startTime: Long,
        endTime: Long,
    ): AppResult<Map<String, Long>>

    suspend fun getMostReadBooks(
        startTime: Long,
        endTime: Long,
        limit: Int,
    ): AppResult<List<BookReadingStatsDomainModel>>

    suspend fun getReadingDays(sinceTimestamp: Long): AppResult<List<Long>>

    suspend fun deleteAllSessions(): CompletableResult
}

