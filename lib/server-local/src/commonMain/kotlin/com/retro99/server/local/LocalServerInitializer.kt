package com.retro99.server.local

import com.retro99.base.AppInitializer
import com.retro99.base.server.ServerType
import com.retro99.server.api.ServerCredentials
import com.retro99.server.api.ServerRegistry
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Initializer that ensures the Local server is always registered.
 * 
 * Unlike remote servers that require login, the Local server is always
 * available for accessing locally imported books.
 */
@Single(binds = [AppInitializer::class])
class LocalServerInitializer(
    @Provided private val serverRegistry: ServerRegistry,
) : AppInitializer {

    override fun initialize() {
        runBlocking {
            ensureLocalServerRegistered()
        }
    }

    private suspend fun ensureLocalServerRegistered() {
        // Check if Local server already exists
        val existingServers = serverRegistry.getAllServers()
        val localServer = existingServers.find { it.type == ServerType.Local }

        if (localServer == null) {
            // Register the Local server
            val config = serverRegistry.addServer(
                name = LocalAuthenticator.LOCAL_SERVER_NAME,
                type = ServerType.Local,
                baseUrl = "local://", // Local files don't need a URL
            )

            // Save dummy credentials so it appears as "authenticated"
            serverRegistry.saveCredentials(
                ServerCredentials(
                    serverId = config.id,
                    username = "local",
                    accessToken = "local-access",
                    refreshToken = null,
                    expiresAt = null,
                )
            )
        }
    }
}

