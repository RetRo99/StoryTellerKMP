package com.retro99.server.storyteller

import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerBooksRepositoryFactory
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerType
import com.retro99.server.implementation.CompositeNetworkClientFactory
import com.retro99.server.storyteller.source.ServerBooksLocalSource
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Factory for creating StorytellerBooksRepository instances.
 * Binds to ServerBooksRepositoryFactory so it can be collected into a list
 * by CompositeServerBooksRepositoryFactory.
 */
@Single(binds = [ServerBooksRepositoryFactory::class])
class StorytellerBooksRepositoryFactory(
    @Provided private val networkClientFactory: CompositeNetworkClientFactory,
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

