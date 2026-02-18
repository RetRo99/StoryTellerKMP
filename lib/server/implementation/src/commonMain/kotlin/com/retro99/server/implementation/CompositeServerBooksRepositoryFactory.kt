package com.retro99.server.implementation

import com.retro99.base.server.ServerType
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerBooksRepositoryFactory
import com.retro99.server.api.ServerConfig
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Composite factory that delegates to the appropriate server-specific factory
 * based on the server type.
 *
 * This factory routes requests to the appropriate server-specific factory
 * based on the server type in the config.
 *
 * Note: This class does NOT implement ServerBooksRepositoryFactory to avoid
 * ambiguous injection. It's injected by its concrete type.
 */
@Single
class CompositeServerBooksRepositoryFactory(
    @Provided private val factories: List<ServerBooksRepositoryFactory>,
) {

    private val factoryMap: Map<ServerType, ServerBooksRepositoryFactory> by lazy {
        factories.associateBy { it.serverType }
    }

    fun create(serverConfig: ServerConfig): ServerBooksRepository {
        val factory = factoryMap[serverConfig.type]
            ?: throw IllegalArgumentException("No factory registered for server type: ${serverConfig.type}")
        return factory.create(serverConfig)
    }
}

