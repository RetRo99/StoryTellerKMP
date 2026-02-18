package com.retro99.server.implementation

import com.retro99.base.server.ServerType
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerSpecificFactory

/**
 * Abstract composite factory that delegates to the appropriate server-specific factory
 * based on the server type.
 *
 * This factory routes requests to the appropriate server-specific factory
 * based on the server type in the config.
 *
 * @param F The type of server-specific factory.
 * @param R The type of repository created by the factories.
 */
abstract class CompositeRepositoryFactory<F : ServerSpecificFactory<R>, R>(
    factories: List<F>,
) {
    private val factoryMap: Map<ServerType, F> by lazy {
        factories.associateBy { it.serverType }
    }

    protected abstract val factoryName: String

    fun create(serverConfig: ServerConfig): R {
        val factory = factoryMap[serverConfig.type]
            ?: throw IllegalArgumentException("No $factoryName registered for server type: ${serverConfig.type}")
        return factory.create(serverConfig)
    }
}

