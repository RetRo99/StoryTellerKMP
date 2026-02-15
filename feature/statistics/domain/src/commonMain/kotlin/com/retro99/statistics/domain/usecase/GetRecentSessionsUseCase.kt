package com.retro99.statistics.domain.usecase

import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import com.retro99.statistics.domain.StatisticsRepository
import com.retro99.statistics.domain.model.ReadingSessionDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting recent reading sessions.
 */
@Factory
class GetRecentSessionsUseCase(
    @Provided private val repository: StatisticsRepository,
) {
    companion object {
        private const val DEFAULT_LIMIT = 50
    }

    /**
     * Gets recent reading sessions, sorted by start time descending.
     *
     * @param limit Maximum number of sessions to return
     * @return List of recent reading sessions
     */
    suspend operator fun invoke(limit: Int = DEFAULT_LIMIT): AppResult<List<ReadingSessionDomainModel>> {
        return repository.getAllSessions().map { sessions ->
            sessions.take(limit)
        }
    }
}

