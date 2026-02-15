package com.retro99.statistics.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.base.result.runCatchingAsAppError
import com.retro99.database.api.statistics.BookReadingStatsEntity
import com.retro99.database.api.statistics.DailyReadingTimeEntity
import com.retro99.database.api.statistics.ReadingSessionDatabase
import com.retro99.database.api.statistics.ReadingSessionEntity
import com.retro99.statistics.data.model.toDomain
import com.retro99.statistics.data.model.toLocal
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel
import com.retro99.statistics.domain.model.DailyReadingTimeDomainModel
import com.retro99.statistics.domain.model.ReadingSessionDomainModel
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.BaseUrlProvider

@Single(binds = [StatisticsLocalSource::class])
internal class StatisticsLocalDataSource(
    @Provided private val database: ReadingSessionDatabase,
    @Provided private val baseUrlProvider: BaseUrlProvider,
) : StatisticsLocalSource {

    override suspend fun insertSession(session: ReadingSessionDomainModel): CompletableResult {
        return runCatchingAsAppError {
            database.insertSession(session.toLocal())
        }
    }

    override suspend fun getAllSessions(): AppResult<List<ReadingSessionDomainModel>> {
        return runCatchingAsAppError {
            database.getAllSessions().map { it.toDomain() }
        }
    }

    override suspend fun getSessionsByBookUuid(
        bookUuid: String,
    ): AppResult<List<ReadingSessionDomainModel>> {
        return runCatchingAsAppError {
            database.getSessionsByBookUuid(bookUuid).map { it.toDomain() }
        }
    }

    override suspend fun getSessionsInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<List<ReadingSessionDomainModel>> {
        return runCatchingAsAppError {
            database.getSessionsInDateRange(startTime, endTime).map { it.toDomain() }
        }
    }

    override suspend fun getTotalReadingTimeMs(): AppResult<Long> {
        return runCatchingAsAppError {
            database.getTotalReadingTimeMs()
        }
    }

    override suspend fun getTotalReadingTimeMsInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<Long> {
        return runCatchingAsAppError {
            database.getTotalReadingTimeMsInDateRange(startTime, endTime)
        }
    }

    override suspend fun getSessionCountInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<Long> {
        return runCatchingAsAppError {
            database.getSessionCountInDateRange(startTime, endTime)
        }
    }

    override suspend fun getDistinctBooksReadInDateRange(
        startTime: Long,
        endTime: Long,
    ): AppResult<Long> {
        return runCatchingAsAppError {
            database.getDistinctBooksReadInDateRange(startTime, endTime)
        }
    }

    override suspend fun getDailyReadingTime(
        sinceTimestamp: Long,
    ): AppResult<List<DailyReadingTimeDomainModel>> {
        return runCatchingAsAppError {
            database.getDailyReadingTime(sinceTimestamp).map { it.toDomain() }
        }
    }

    override suspend fun getReadingTimeByBookType(
        startTime: Long,
        endTime: Long,
    ): AppResult<Map<String, Long>> {
        return runCatchingAsAppError {
            database.getReadingTimeByBookType(startTime, endTime)
        }
    }

    override suspend fun getMostReadBooks(
        startTime: Long,
        endTime: Long,
        limit: Int,
    ): AppResult<List<BookReadingStatsDomainModel>> {
        val baseUrl = baseUrlProvider.getBaseUrl()
        return runCatchingAsAppError {
            database.getMostReadBooks(startTime, endTime, limit).map { it.toDomain(baseUrl) }
        }
    }

    override suspend fun getReadingDays(sinceTimestamp: Long): AppResult<List<Long>> {
        return runCatchingAsAppError {
            database.getReadingDays(sinceTimestamp)
        }
    }

    override suspend fun deleteAllSessions(): CompletableResult {
        return runCatchingAsAppError {
            database.deleteAllSessions()
        }
    }
}

