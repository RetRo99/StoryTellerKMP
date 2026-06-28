package com.retro99.server.implementation

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerNetworkClientFactory
import com.retro99.server.api.ServerRegistry
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class CompositeNetworkClientFactory(
    @Provided private val factories: List<ServerNetworkClientFactory>,
    @Provided private val serverRegistry: ServerRegistry,
) {
    private val factoryMap: Map<com.retro99.base.server.ServerType, ServerNetworkClientFactory> by lazy {
        factories.associateBy { it.serverType }
    }

    fun create(serverConfig: ServerConfig): ServerNetworkClient {
        val factory = factoryMap[serverConfig.type]
            ?: throw IllegalArgumentException(
                "No network client factory registered for server type: ${serverConfig.type}",
            )
        return factory.create(serverConfig)
    }

    suspend fun createForActiveServer(): ServerNetworkClient {
        val activeServer = serverRegistry.getActiveServer()
            ?: error("No active server configured")
        return create(activeServer)
    }
}
