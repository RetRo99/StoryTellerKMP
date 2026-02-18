package com.retro99.settings.data

import com.retro99.base.server.ServerType
import com.retro99.server.api.ServerRegistry
import com.retro99.settings.domain.SettingsRepository
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [SettingsRepository::class])
internal class SettingsDataRepository(
    @Provided private val serverRegistry: ServerRegistry,
) : SettingsRepository {

    override suspend fun logout(serverId: String?) {
        if (serverId != null) {
            serverRegistry.clearCredentials(serverId)
        } else {
            // Logout from all remote servers (excludes local server)
            val servers = serverRegistry.getAllServers()
            servers
                .filter { it.type != ServerType.Local }
                .forEach { server ->
                    serverRegistry.clearCredentials(server.id)
                }
        }
    }
}

