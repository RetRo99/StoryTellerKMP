package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.data.model.SeriesApiModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.NetworkClient
import retro99.network.api.get

@Factory(binds = [SeriesRemoteSource::class])
internal class SeriesRemoteDataSource(
    @Provided private val networkClient: NetworkClient,
) : SeriesRemoteSource {

    override suspend fun getSeries(): AppResult<List<SeriesApiModel>> {
        return networkClient.get(path = "/api/v2/series")
    }
}

