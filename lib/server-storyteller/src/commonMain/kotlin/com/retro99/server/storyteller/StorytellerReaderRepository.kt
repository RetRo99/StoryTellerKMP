package com.retro99.server.storyteller

import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerPosition
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.storyteller.model.StorytellerPositionApiModel
import com.retro99.server.storyteller.model.toServerPosition
import com.retro99.server.storyteller.model.toStorytellerApiModel
import retro99.network.api.get
import retro99.network.api.post

/**
 * Storyteller implementation of ServerReaderRepository.
 * Handles reading progress sync with a Storyteller server using the v2 API.
 */
class StorytellerReaderRepository(
    private val networkClient: ServerNetworkClient,
) : ServerReaderRepository {

    override val serverId: String = networkClient.serverId

    override suspend fun getPosition(bookUuid: String): AppResult<ServerPosition?> {
        return networkClient.get<StorytellerPositionApiModel?>(
            path = "/api/v2/books/$bookUuid/positions"
        ).map { apiModel ->
            apiModel?.toServerPosition(bookUuid, serverId)
        }
    }

    override suspend fun updatePosition(bookUuid: String, position: ServerPosition): CompletableResult {
        return networkClient.post(
            path = "/api/v2/books/$bookUuid/positions",
            body = position.toStorytellerApiModel()
        )
    }
}

