package com.retro99.server.api

import com.retro99.base.result.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Server-aware series repository interface.
 * Each server type implements this with its own API.
 */
interface ServerSeriesRepository {
    /**
     * The server ID this repository is associated with.
     */
    val serverId: String

    /**
     * Get all series from this server.
     */
    fun getSeries(): Flow<AppResult<List<ServerSeries>>>
}

/**
 * A series associated with a specific server.
 */
data class ServerSeries(
    val uuid: String,
    val serverId: String,
    val name: String,
    val featured: Int?,
    val position: Double?,
    val createdAt: String?,
    val updatedAt: String?,
)

