package com.retro99.server.local

import com.retro99.base.AppInitializer
import com.retro99.base.server.LOCAL_SERVER_ID
import com.retro99.base.server.ServerType
import com.retro99.server.api.ServerCredentials
import com.retro99.server.api.ServerRegistry
import com.retro99.user.api.UserRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Initializer that ensures the Local server is always registered for each user.
 *
 * Unlike remote servers that require login, the Local server is always
 * available for accessing locally imported books.
 *
 * This initializer observes both user profile changes AND server list changes
 * to ensure the Local server is always present. This handles race conditions
 * where ServerRegistry might reload and clear the Local server.
 */
@Single(binds = [AppInitializer::class])
class LocalServerInitializer(
    @Provided private val serverRegistry: ServerRegistry,
    @Provided private val userRegistry: UserRegistry,
) : AppInitializer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun initialize() {
        // Ensure Local server exists for initial user
        runBlocking {
            ensureLocalServerRegistered()
        }

        // Observe BOTH user changes AND server list changes
        // This ensures Local server is re-added if ServerRegistry reloads and clears it
        combine(
            userRegistry.observeActiveProfile(),
            serverRegistry.observeAllServers()
        ) { profile, servers ->
            val hasLocalServer = servers.any { it.id == LOCAL_SERVER_ID }
            profile != null && !hasLocalServer
        }
            .onEach { needsLocalServer ->
                if (needsLocalServer) {
                    ensureLocalServerRegistered()
                }
            }
            .launchIn(scope)
    }

    private suspend fun ensureLocalServerRegistered() {
        // Check if Local server already exists by its fixed ID
        val existingServer = serverRegistry.getServer(LOCAL_SERVER_ID)

        if (existingServer == null) {
            // Register the Local server with a fixed ID
            serverRegistry.addServerWithId(
                id = LOCAL_SERVER_ID,
                name = LocalAuthenticator.LOCAL_SERVER_NAME,
                type = ServerType.Local,
                baseUrl = "local://", // Local files don't need a URL
            )
        }

        // Always ensure credentials exist for Local server
        // This handles the case where server exists but credentials were cleared
        if (!serverRegistry.isAuthenticated(LOCAL_SERVER_ID)) {
            serverRegistry.saveCredentials(
                ServerCredentials(
                    serverId = LOCAL_SERVER_ID,
                    username = "local",
                    accessToken = "local-access",
                    refreshToken = null,
                    expiresAt = null,
                )
            )
        }
    }
}

