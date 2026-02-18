package com.retro99.server.local

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.api.ServerReaderRepositoryFactory
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Single

/**
 * Factory for creating LocalReaderRepository instances.
 */
@Single
class LocalReaderRepositoryFactory : ServerReaderRepositoryFactory {

    override val serverType: ServerType = ServerType.Local

    override fun create(serverConfig: ServerConfig): ServerReaderRepository {
        require(serverConfig.type == ServerType.Local) {
            "LocalReaderRepositoryFactory can only create repositories for Local servers"
        }
        return LocalReaderRepository(serverConfig.id)
    }
}

