package com.retro99.server.storyteller

import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerNetworkClientFactory
import com.retro99.server.api.ServerRegistry
import com.retro99.server.api.ServerTokenProvider
import com.retro99.server.api.ServerType
import io.ktor.client.HttpClient
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class StorytellerNetworkClientFactory(
    @Provided private val httpClient: HttpClient,
    @Provided private val tokenProvider: ServerTokenProvider,
    @Provided private val serverRegistry: ServerRegistry,
) : ServerNetworkClientFactory {

    override val serverType: ServerType = ServerType.Storyteller

    override fun create(serverConfig: ServerConfig): ServerNetworkClient {
        require(serverConfig.type == ServerType.Storyteller) {
            "StorytellerNetworkClientFactory can only create clients for Storyteller servers"
        }
        return StorytellerNetworkClient(
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            serverConfig = serverConfig,
        )
    }

    override suspend fun createForActiveServer(): ServerNetworkClient {
        val activeServer = serverRegistry.getActiveServer()
            ?: error("No active server configured")
        return create(activeServer)
    }
}

