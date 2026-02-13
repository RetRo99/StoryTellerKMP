package com.retro99.books.data

import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppResult
import com.retro99.base.result.mapCatching
import com.retro99.books.data.model.toDomain
import com.retro99.books.data.model.toSeriesLocal
import com.retro99.books.data.source.SeriesLocalSource
import com.retro99.books.data.source.SeriesRemoteSource
import com.retro99.books.domain.SeriesRepository
import com.retro99.books.domain.model.SeriesDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [SeriesRepository::class])
internal class SeriesDataRepository(
    @Provided private val remoteSource: SeriesRemoteSource,
    @Provided private val localSource: SeriesLocalSource,
) : SeriesRepository, BaseRepository {

    override fun getSeries(): Flow<AppResult<List<SeriesDomainModel>>> {
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getSeries().mapCatching { series ->
                    series?.map { it.toDomain() }
                }
            },
            remoteSource = {
                remoteSource.getSeries().mapCatching { seriesList ->
                    seriesList.map { it.toDomain() }
                        .sortedBy { it.name.lowercase() }
                }
            },
            saveToCache = { domainSeries ->
                localSource.saveSeries(domainSeries.map { it.toSeriesLocal() })
            },
        )
    }
}

