package com.retro99.server.audiobookshelf

import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerSeriesRepository
import com.retro99.server.audiobookshelf.model.AudiobookshelfLibraryListApiModel
import com.retro99.server.audiobookshelf.model.AudiobookshelfSeriesListResponse
import com.retro99.server.audiobookshelf.model.toServerSeries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retro99.network.api.get

class AudiobookshelfSeriesRepository(
    private val networkClient: ServerNetworkClient,
) : ServerSeriesRepository {

    override val serverId: String = networkClient.serverId

    override fun getSeries(): Flow<AppResult<List<com.retro99.server.api.ServerSeries>>> = flow {
        val result = networkClient.get<AudiobookshelfLibraryListApiModel>(
            path = "/api/libraries",
        ).map { libraryList ->
            libraryList.libraries.flatMap { library ->
                networkClient.get<AudiobookshelfSeriesListResponse>(
                    path = "/api/libraries/${library.id}/series",
                    queryBuilder = {
                        "limit" to "0"
                    },
                ).map { response ->
                    response.results.map { series -> series.toServerSeries(serverId) }
                }.getOrElse { emptyList() }
            }.sortedBy { series -> series.name.lowercase() }
        }
        emit(result)
    }
}
