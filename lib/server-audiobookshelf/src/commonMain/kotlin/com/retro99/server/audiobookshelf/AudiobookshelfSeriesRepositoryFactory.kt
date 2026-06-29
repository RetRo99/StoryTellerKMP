package com.retro99.server.audiobookshelf

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerSeriesRepository
import com.retro99.server.api.ServerSeriesRepositoryFactory
import com.retro99.server.api.ServerNetworkClientProvider
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [ServerSeriesRepositoryFactory::class])
class AudiobookshelfSeriesRepositoryFactory(
    @Provided private val networkClientFactory: ServerNetworkClientProvider,
) : ServerSeriesRepositoryFactory {

    override val serverType: ServerType = ServerType.Audiobookshelf

    override fun create(serverConfig: ServerConfig): ServerSeriesRepository {
        require(serverConfig.type == ServerType.Audiobookshelf) {
            "AudiobookshelfSeriesRepositoryFactory can only create repositories for Audiobookshelf servers"
        }
        val networkClient = networkClientFactory.create(serverConfig)
        return AudiobookshelfSeriesRepository(networkClient)
    }
}
