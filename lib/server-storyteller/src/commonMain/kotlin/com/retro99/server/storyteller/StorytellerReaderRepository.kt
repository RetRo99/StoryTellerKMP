package com.retro99.server.storyteller

import com.github.michaelbull.result.map
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerPosition
import com.retro99.server.api.ServerPositionLocalSource
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.storyteller.model.StorytellerPositionApiModel
import com.retro99.server.storyteller.model.toServerPosition
import com.retro99.server.storyteller.model.toStorytellerApiModel
import retro99.network.api.get
import retro99.network.api.post

/**
 * Storyteller implementation of ServerReaderRepository.
 * Handles reading progress sync with a Storyteller server using the v2 API.
 * Owns both local cache and remote sync operations.
 */
class StorytellerReaderRepository(
    private val networkClient: ServerNetworkClient,
    private val localSource: ServerPositionLocalSource,
) : ServerReaderRepository, BaseRepository {

    override val serverId: String = networkClient.serverId

    // ==================== Combined Operations ====================

    override suspend fun getPosition(bookUuid: String): AppResult<ServerPosition?> {
        return remoteWithCacheFallback(
            remoteSource = { getRemotePosition(bookUuid) },
            cacheSource = { getLocalPosition(bookUuid) },
            saveToCache = { position -> localSource.savePosition(position) },
        )
    }

    override suspend fun savePosition(bookUuid: String, position: ServerPosition): CompletableResult {
        // Save to local cache first (errors logged by BaseRepository pattern)
        localSource.savePosition(position)

        // Then sync to remote
        return networkClient.post(
            path = "/api/v2/books/$bookUuid/positions",
            body = position.toStorytellerApiModel()
        )
    }

    // ==================== Local-only Operations ====================

    override suspend fun getLocalPosition(bookUuid: String): AppResult<ServerPosition?> {
        return localSource.getPosition(bookUuid).map { position ->
            // Set the serverId since it's not stored in the database
            position?.copy(serverId = serverId)
        }
    }

    override suspend fun saveLocalPosition(position: ServerPosition): CompletableResult {
        return localSource.savePosition(position)
    }

    // ==================== Remote-only Operations ====================

    override suspend fun getRemotePosition(bookUuid: String): AppResult<ServerPosition?> {
        return networkClient.get<StorytellerPositionApiModel?>(
            path = "/api/v2/books/$bookUuid/positions"
        ).map { apiModel ->
            apiModel?.toServerPosition(bookUuid, serverId)
        }
    }
}

