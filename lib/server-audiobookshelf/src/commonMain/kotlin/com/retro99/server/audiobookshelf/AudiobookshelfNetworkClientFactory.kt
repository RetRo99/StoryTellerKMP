package com.retro99.server.audiobookshelf

import com.retro99.network.implementation.ServerNetworkClientBuilder
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerNetworkClientFactory
import com.retro99.server.api.ServerTokenProvider
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class AudiobookshelfNetworkClientFactory(
    @Provided private val serverNetworkClientBuilder: ServerNetworkClientBuilder,
    @Provided private val tokenProvider: ServerTokenProvider,
) : ServerNetworkClientFactory {

    override val serverType: ServerType = ServerType.Audiobookshelf

    override fun create(serverConfig: ServerConfig): ServerNetworkClient {
        require(serverConfig.type == ServerType.Audiobookshelf) {
            "AudiobookshelfNetworkClientFactory can only create clients for Audiobookshelf servers"
        }
        return serverNetworkClientBuilder.build(
            serverId = serverConfig.id,
            baseUrl = serverConfig.baseUrl,
            tokenProvider = { tokenProvider.getToken(serverConfig.id) },
        )
    }
}
