package com.retro99.statistics.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.statistics.domain.StatisticsRepository
import com.retro99.statistics.domain.model.DailyReadingTimeDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting daily reading time for the last N days.
 */
@Factory
class GetDailyReadingTimeUseCase(
    @Provided private val repository: StatisticsRepository,
) {
    suspend operator fun invoke(days: Int = 30): AppResult<List<DailyReadingTimeDomainModel>> {
        return repository.getDailyReadingTime(days)
    }
}

