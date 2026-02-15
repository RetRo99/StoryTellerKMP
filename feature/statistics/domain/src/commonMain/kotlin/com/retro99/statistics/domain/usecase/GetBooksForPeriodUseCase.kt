package com.retro99.statistics.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.statistics.domain.StatisticsRepository
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel
import com.retro99.statistics.domain.model.StatisticsPeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import kotlin.time.Clock
import kotlin.time.Clock.System.now

/**
 * Use case for getting the most read books within a specific time period.
 */
@Factory
class GetBooksForPeriodUseCase(
    @Provided private val repository: StatisticsRepository,
) {
    companion object {
        private const val DEFAULT_LIMIT = 20
    }

    /**
     * Gets the most read books for the specified period.
     *
     * @param period The time period to filter by
     * @param limit Maximum number of books to return
     * @return List of book reading statistics for the period
     */
    suspend operator fun invoke(
        period: StatisticsPeriod,
        limit: Int = DEFAULT_LIMIT,
    ): AppResult<List<BookReadingStatsDomainModel>> {
        val now = now()
        val timeZone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timeZone).date
        val todayStartInstant = today.atStartOfDayIn(timeZone)

        val startTime = when (period) {
            StatisticsPeriod.TODAY -> todayStartInstant.toEpochMilliseconds()
            StatisticsPeriod.WEEK -> today.minus(1, DateTimeUnit.WEEK)
                .atStartOfDayIn(timeZone).toEpochMilliseconds()
            StatisticsPeriod.MONTH -> today.minus(1, DateTimeUnit.MONTH)
                .atStartOfDayIn(timeZone).toEpochMilliseconds()
            StatisticsPeriod.TOTAL -> 0L
        }

        return repository.getMostReadBooksInDateRange(
            startTime = startTime,
            endTime = now.toEpochMilliseconds(),
            limit = limit,
        )
    }
}

