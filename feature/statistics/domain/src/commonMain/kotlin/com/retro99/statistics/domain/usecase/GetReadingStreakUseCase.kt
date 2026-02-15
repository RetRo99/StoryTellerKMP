package com.retro99.statistics.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.statistics.domain.StatisticsRepository
import com.retro99.statistics.domain.model.ReadingStreakDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting reading streak information.
 */
@Factory
class GetReadingStreakUseCase(
    @Provided private val repository: StatisticsRepository,
) {
    suspend operator fun invoke(): AppResult<ReadingStreakDomainModel> {
        return repository.getReadingStreak()
    }
}

