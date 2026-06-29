package com.retro99.server.storyteller

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerPositionLocalSource
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.api.ServerReaderRepositoryFactory
import com.retro99.server.api.ServerNetworkClientProvider
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [ServerReaderRepositoryFactory::class])
class StorytellerReaderRepositoryFactory(
    @Provided private val networkClientFactory: ServerNetworkClientProvider,
    @Provided private val localSource: ServerPositionLocalSource,
) : ServerReaderRepositoryFactory {

    override val serverType: ServerType = ServerType.Storyteller

    override fun create(serverConfig: ServerConfig): ServerReaderRepository {
        require(serverConfig.type == ServerType.Storyteller) {
            "StorytellerReaderRepositoryFactory can only create repositories for Storyteller servers"
        }
        val networkClient = networkClientFactory.create(serverConfig)
        return StorytellerReaderRepository(networkClient, localSource)
    }
}

