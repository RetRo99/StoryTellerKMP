package com.retro99.server.implementation

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.api.ServerReaderRepositoryFactory
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Composite factory that delegates to the appropriate server-specific factory
 * based on the server type.
 *
 * This factory collects all server-specific reader factories and routes requests
 * to the appropriate one based on the server type.
 */
@Single(binds = [ServerReaderRepositoryFactory::class])
class CompositeServerReaderRepositoryFactory(
    @Provided private val factories: List<ServerReaderRepositoryFactory>,
) : ServerReaderRepositoryFactory {

    private val factoryMap: Map<ServerType, ServerReaderRepositoryFactory> by lazy {
        factories.associateBy { it.serverType }
    }

    override val serverType: ServerType
        get() = throw UnsupportedOperationException("CompositeServerReaderRepositoryFactory handles all server types")

    override fun create(serverConfig: ServerConfig): ServerReaderRepository {
        val factory = factoryMap[serverConfig.type]
            ?: throw IllegalArgumentException("No reader factory registered for server type: ${serverConfig.type}")
        return factory.create(serverConfig)
    }
}

