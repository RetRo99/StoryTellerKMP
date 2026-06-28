package com.retro99.server.storyteller

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerSeriesRepository
import com.retro99.server.api.ServerSeriesRepositoryFactory
import com.retro99.server.api.ServerType
import com.retro99.server.implementation.CompositeNetworkClientFactory
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Factory for creating StorytellerSeriesRepository instances.
 * Binds to ServerSeriesRepositoryFactory so it can be collected into a list
 * by CompositeServerSeriesRepositoryFactory.
 */
@Single(binds = [ServerSeriesRepositoryFactory::class])
class StorytellerSeriesRepositoryFactory(
    @Provided private val networkClientFactory: CompositeNetworkClientFactory,
) : ServerSeriesRepositoryFactory {

    override val serverType: ServerType = ServerType.Storyteller

    override fun create(serverConfig: ServerConfig): ServerSeriesRepository {
        require(serverConfig.type == ServerType.Storyteller) {
            "StorytellerSeriesRepositoryFactory can only create repositories for Storyteller servers"
        }
        val networkClient = networkClientFactory.create(serverConfig)
        return StorytellerSeriesRepository(networkClient)
    }
}

