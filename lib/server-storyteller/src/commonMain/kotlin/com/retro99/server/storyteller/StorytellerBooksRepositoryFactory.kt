package com.retro99.server.storyteller

import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerBooksRepositoryFactory
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerNetworkClientFactory
import com.retro99.server.api.ServerType
import com.retro99.server.storyteller.source.ServerBooksLocalSource
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class StorytellerBooksRepositoryFactory(
    @Provided private val networkClientFactory: ServerNetworkClientFactory,
    @Provided private val localSource: ServerBooksLocalSource,
) : ServerBooksRepositoryFactory {

    override val serverType: ServerType = ServerType.Storyteller

    override fun create(serverConfig: ServerConfig): ServerBooksRepository {
        require(serverConfig.type == ServerType.Storyteller) {
            "StorytellerBooksRepositoryFactory can only create repositories for Storyteller servers"
        }
        val networkClient = networkClientFactory.create(serverConfig)
        return StorytellerBooksRepository(networkClient, localSource)
    }
}

