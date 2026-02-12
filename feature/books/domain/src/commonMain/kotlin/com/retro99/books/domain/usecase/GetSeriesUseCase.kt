package com.retro99.books.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.books.domain.SeriesRepository
import com.retro99.books.domain.model.SeriesDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetSeriesUseCase(
    @Provided private val seriesRepository: SeriesRepository,
) {
    operator fun invoke(): Flow<AppResult<List<SeriesDomainModel>>> {
        return seriesRepository.getSeries()
    }
}

