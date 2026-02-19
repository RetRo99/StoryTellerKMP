package com.retro99.server.storyteller

import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerSeries
import com.retro99.server.api.ServerSeriesRepository
import com.retro99.server.storyteller.model.StorytellerSeriesListApiModel
import com.retro99.server.storyteller.model.toServerSeries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retro99.network.api.get

/**
 * Storyteller implementation of ServerSeriesRepository.
 * Fetches series from a Storyteller server using the v2 API.
 */
class StorytellerSeriesRepository(
    private val networkClient: ServerNetworkClient,
) : ServerSeriesRepository {

    override val serverId: String = networkClient.serverId

    override fun getSeries(): Flow<AppResult<List<ServerSeries>>> = flow {
        val result = networkClient.get<List<StorytellerSeriesListApiModel>>(
            path = "/api/v2/series"
        ).map { seriesList ->
            seriesList.map { it.toServerSeries(serverId) }
                .sortedBy { it.name.lowercase() }
        }
        emit(result)
    }
}

