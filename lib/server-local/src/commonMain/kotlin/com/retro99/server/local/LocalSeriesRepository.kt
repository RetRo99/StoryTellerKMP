package com.retro99.server.local

import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppResult
import com.retro99.server.api.ServerSeries
import com.retro99.server.api.ServerSeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Local implementation of ServerSeriesRepository.
 * Local books don't have series, so this always returns an empty list.
 */
class LocalSeriesRepository(
    override val serverId: String,
) : ServerSeriesRepository {

    override fun getSeries(): Flow<AppResult<List<ServerSeries>>> {
        // Local books don't have series
        return flowOf(Ok(emptyList()))
    }
}

