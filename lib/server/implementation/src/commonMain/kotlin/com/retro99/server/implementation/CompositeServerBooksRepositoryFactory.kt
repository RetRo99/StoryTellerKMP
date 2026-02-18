package com.retro99.server.implementation

import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerBooksRepositoryFactory
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Composite factory that delegates to the appropriate server-specific factory
 * based on the server type.
 *
 * This factory collects all server-specific factories and routes requests
 * to the appropriate one based on the server type.
 */
@Single(binds = [ServerBooksRepositoryFactory::class])
class CompositeServerBooksRepositoryFactory(
    @Provided private val factories: List<ServerBooksRepositoryFactory>,
) : ServerBooksRepositoryFactory {

    private val factoryMap: Map<ServerType, ServerBooksRepositoryFactory> by lazy {
        factories.associateBy { it.serverType }
    }

    override val serverType: ServerType
        get() = throw UnsupportedOperationException("CompositeServerBooksRepositoryFactory handles all server types")

    override fun create(serverConfig: ServerConfig): ServerBooksRepository {
        val factory = factoryMap[serverConfig.type]
            ?: throw IllegalArgumentException("No factory registered for server type: ${serverConfig.type}")
        return factory.create(serverConfig)
    }
}

