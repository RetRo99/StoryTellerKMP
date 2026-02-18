package com.retro99.server.implementation

import com.retro99.base.server.ServerType
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.api.ServerReaderRepositoryFactory
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Composite factory that delegates to the appropriate server-specific factory
 * based on the server type.
 *
 * This factory routes requests to the appropriate server-specific factory
 * based on the server type in the config.
 *
 * Note: This class does NOT implement ServerReaderRepositoryFactory to avoid
 * ambiguous injection. It's injected by its concrete type.
 */
@Single
class CompositeServerReaderRepositoryFactory(
    @Provided private val factories: List<ServerReaderRepositoryFactory>,
) {

    private val factoryMap: Map<ServerType, ServerReaderRepositoryFactory> by lazy {
        factories.associateBy { it.serverType }
    }

    fun create(serverConfig: ServerConfig): ServerReaderRepository {
        val factory = factoryMap[serverConfig.type]
            ?: throw IllegalArgumentException("No reader factory registered for server type: ${serverConfig.type}")
        return factory.create(serverConfig)
    }
}

