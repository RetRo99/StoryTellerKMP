package com.retro99.server.local

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerSeriesRepository
import com.retro99.server.api.ServerSeriesRepositoryFactory
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Single

/**
 * Factory for creating LocalSeriesRepository instances.
 * Binds to ServerSeriesRepositoryFactory so it can be collected into a list
 * by CompositeServerSeriesRepositoryFactory.
 */
@Single(binds = [ServerSeriesRepositoryFactory::class])
class LocalSeriesRepositoryFactory : ServerSeriesRepositoryFactory {

    override val serverType: ServerType = ServerType.Local

    override fun create(serverConfig: ServerConfig): ServerSeriesRepository {
        require(serverConfig.type == ServerType.Local) {
            "LocalSeriesRepositoryFactory can only create repositories for Local servers"
        }
        return LocalSeriesRepository(serverConfig.id)
    }
}

