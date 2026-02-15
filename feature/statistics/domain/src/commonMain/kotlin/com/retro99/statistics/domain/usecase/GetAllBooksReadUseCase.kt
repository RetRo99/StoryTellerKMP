package com.retro99.statistics.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.statistics.domain.StatisticsRepository
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting all books that have been read.
 */
@Factory
class GetAllBooksReadUseCase(
    @Provided private val repository: StatisticsRepository,
) {
    companion object {
        private const val ALL_BOOKS_LIMIT = 100
    }

    /**
     * Gets all books that have been read, sorted by total reading time.
     *
     * @return List of book reading statistics
     */
    suspend operator fun invoke(): AppResult<List<BookReadingStatsDomainModel>> {
        return repository.getMostReadBooks(ALL_BOOKS_LIMIT)
    }
}

