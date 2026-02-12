package com.retro99.books.data

import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.toDomain
import com.retro99.books.data.source.SeriesRemoteSource
import com.retro99.books.domain.SeriesRepository
import com.retro99.books.domain.model.SeriesDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [SeriesRepository::class])
internal class SeriesDataRepository(
    @Provided private val remoteSource: SeriesRemoteSource,
) : SeriesRepository {

    override fun getSeries(): Flow<AppResult<List<SeriesDomainModel>>> = flow {
        val result = remoteSource.getSeries().map { seriesList ->
            seriesList.map { it.toDomain() }
                .sortedBy { it.name.lowercase() }
        }
        emit(result)
    }
}

