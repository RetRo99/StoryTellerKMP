package com.retro99.statistics.data

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.retro99.base.nowMillis
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.model.BookType
import com.retro99.statistics.data.source.StatisticsLocalSource
import com.retro99.statistics.domain.StatisticsRepository
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel
import com.retro99.statistics.domain.model.DailyReadingTimeDomainModel
import com.retro99.statistics.domain.model.ReadingSessionDomainModel
import com.retro99.statistics.domain.model.ReadingStatisticsDomainModel
import com.retro99.statistics.domain.model.ReadingStreakDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.collections.mapKeys

@Single(binds = [StatisticsRepository::class])
internal class StatisticsDataRepository(
    @Provided private val localSource: StatisticsLocalSource,
) : StatisticsRepository {

    companion object {
        private const val MS_PER_DAY = 86400000L
        private const val DAYS_IN_WEEK = 7
        private const val DAYS_IN_MONTH = 30
        private const val DAYS_FOR_CHART = 30
        private const val TOP_BOOKS_LIMIT = 5
    }

    override suspend fun saveReadingSession(
        session: ReadingSessionDomainModel,
    ): CompletableResult {
        return localSource.insertSession(session)
    }

    override suspend fun getAllSessions(): AppResult<List<ReadingSessionDomainModel>> {
        return localSource.getAllSessions()
    }

    override suspend fun getSessionsByBook(
        bookUuid: String,
    ): AppResult<List<ReadingSessionDomainModel>> {
        return localSource.getSessionsByBookUuid(bookUuid)
    }

    override fun getReadingStatistics(): Flow<AppResult<ReadingStatisticsDomainModel>> = flow {
        val now = nowMillis()
        val todayStart = getStartOfDay(now)
        val weekStart = todayStart - (DAYS_IN_WEEK * MS_PER_DAY)
        val monthStart = todayStart - (DAYS_IN_MONTH * MS_PER_DAY)
        val chartStart = todayStart - (DAYS_FOR_CHART * MS_PER_DAY)

        val totalTime = localSource.getTotalReadingTimeMs().getOrElse { 0L }
        val todayTime = localSource.getTotalReadingTimeMsInDateRange(todayStart, now)
            .getOrElse { 0L }
        val weekTime = localSource.getTotalReadingTimeMsInDateRange(weekStart, now)
            .getOrElse { 0L }
        val monthTime = localSource.getTotalReadingTimeMsInDateRange(monthStart, now)
            .getOrElse { 0L }
        val totalSessions = localSource.getSessionCountInDateRange(0, now).getOrElse { 0L }
        val totalBooks = localSource.getDistinctBooksReadInDateRange(0, now).getOrElse { 0L }
        val dailyReadingTime = localSource.getDailyReadingTime(chartStart).getOrElse { emptyList() }
        val mostReadBooks = localSource.getMostReadBooks(0, now, TOP_BOOKS_LIMIT)
            .getOrElse { emptyList() }
        val readingTimeByType = localSource.getReadingTimeByBookType(0, now).getOrElse { emptyMap() }
        val streak = calculateStreak(now)

        val statistics = ReadingStatisticsDomainModel(
            totalReadingTimeMs = totalTime,
            todayReadingTimeMs = todayTime,
            weekReadingTimeMs = weekTime,
            monthReadingTimeMs = monthTime,
            totalSessions = totalSessions,
            totalBooksRead = totalBooks,
            currentStreak = streak.currentStreak,
            longestStreak = streak.longestStreak,
            dailyReadingTime = dailyReadingTime,
            mostReadBooks = mostReadBooks,
            readingTimeByType = readingTimeByType.mapKeys { BookType.fromValue(it.key) },
        )

        emit(Ok(statistics))
    }

    override suspend fun getTotalReadingTimeMs(): AppResult<Long> {
        return localSource.getTotalReadingTimeMs()
    }

    override suspend fun getTodayReadingTimeMs(): AppResult<Long> {
        val now = nowMillis()
        val todayStart = getStartOfDay(now)
        return localSource.getTotalReadingTimeMsInDateRange(todayStart, now)
    }

    override suspend fun getWeekReadingTimeMs(): AppResult<Long> {
        val now = nowMillis()
        val weekStart = getStartOfDay(now) - (DAYS_IN_WEEK * MS_PER_DAY)
        return localSource.getTotalReadingTimeMsInDateRange(weekStart, now)
    }

    override suspend fun getMonthReadingTimeMs(): AppResult<Long> {
        val now = nowMillis()
        val monthStart = getStartOfDay(now) - (DAYS_IN_MONTH * MS_PER_DAY)
        return localSource.getTotalReadingTimeMsInDateRange(monthStart, now)
    }

    override suspend fun getDailyReadingTime(
        days: Int,
    ): AppResult<List<DailyReadingTimeDomainModel>> {
        val now = nowMillis()
        val sinceTimestamp = getStartOfDay(now) - (days * MS_PER_DAY)
        return localSource.getDailyReadingTime(sinceTimestamp)
    }

    override suspend fun getMostReadBooks(
        limit: Int,
    ): AppResult<List<BookReadingStatsDomainModel>> {
        val now = nowMillis()
        return localSource.getMostReadBooks(0, now, limit)
    }

    override suspend fun getReadingTimeByType(): AppResult<Map<BookType, Long>> {
        val now = nowMillis()
        return localSource.getReadingTimeByBookType(0, now).map { typeMap ->
            typeMap.mapKeys { BookType.fromValue(it.key) }
        }
    }

    override suspend fun getReadingStreak(): AppResult<ReadingStreakDomainModel> {
        val now = nowMillis()
        return Ok(calculateStreak(now))
    }

    override suspend fun clearAllSessions(): CompletableResult {
        return localSource.deleteAllSessions()
    }

    private suspend fun calculateStreak(now: Long): ReadingStreakDomainModel {
        val yearAgo = now - (365 * MS_PER_DAY)
        val readingDays = localSource.getReadingDays(yearAgo).getOrElse { emptyList() }

        if (readingDays.isEmpty()) {
            return ReadingStreakDomainModel(0, 0, null)
        }

        val todayDayNumber = now / MS_PER_DAY
        val sortedDays = readingDays.sorted().reversed()
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 1
        var lastDay = sortedDays.first()

        // Check if the most recent reading day is today or yesterday
        val isCurrentStreakActive = lastDay == todayDayNumber || lastDay == todayDayNumber - 1

        for (i in 1 until sortedDays.size) {
            val currentDay = sortedDays[i]
            if (lastDay - currentDay == 1L) {
                tempStreak++
            } else {
                if (isCurrentStreakActive && currentStreak == 0) {
                    currentStreak = tempStreak
                }
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
            lastDay = currentDay
        }

        // Handle the last streak
        if (isCurrentStreakActive && currentStreak == 0) {
            currentStreak = tempStreak
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        return ReadingStreakDomainModel(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastReadingDay = sortedDays.first() * MS_PER_DAY,
        )
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return (timestamp / MS_PER_DAY) * MS_PER_DAY
    }
}

