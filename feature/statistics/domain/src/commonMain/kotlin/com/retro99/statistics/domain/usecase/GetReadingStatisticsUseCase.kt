package com.retro99.statistics.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.statistics.domain.StatisticsRepository
import com.retro99.statistics.domain.model.ReadingStatisticsDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting aggregated reading statistics.
 */
@Factory
class GetReadingStatisticsUseCase(
    @Provided private val repository: StatisticsRepository,
) {
    operator fun invoke(): Flow<AppResult<ReadingStatisticsDomainModel>> {
        return repository.getReadingStatistics()
    }
}

