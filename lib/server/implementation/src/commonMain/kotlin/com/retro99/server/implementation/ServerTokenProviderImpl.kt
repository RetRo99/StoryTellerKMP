package com.retro99.server.implementation

import com.retro99.server.api.ServerRegistry
import com.retro99.server.api.ServerTokenProvider
import kotlin.time.Clock
import org.koin.core.annotation.Single

@Single(binds = [ServerTokenProvider::class])
class ServerTokenProviderImpl(
    private val serverRegistry: ServerRegistry,
) : ServerTokenProvider {

    override suspend fun getToken(serverId: String): String? {
        val credentials = serverRegistry.getCredentials(serverId) ?: return null

        credentials.expiresAt?.let { expiresAt ->
            if (expiresAt < Clock.System.now().toEpochMilliseconds()) {
                return refreshToken(serverId)
            }
        }

        return credentials.accessToken
    }

    override suspend fun getActiveServerToken(): String? {
        val activeServer = serverRegistry.getActiveServer() ?: return null
        return getToken(activeServer.id)
    }

    override suspend fun refreshToken(serverId: String): String? {
        val server = serverRegistry.getServer(serverId) ?: return null
        val credentials = serverRegistry.getCredentials(serverId) ?: return null
        val refreshToken = credentials.refreshToken ?: return null

        return null
    }
}
