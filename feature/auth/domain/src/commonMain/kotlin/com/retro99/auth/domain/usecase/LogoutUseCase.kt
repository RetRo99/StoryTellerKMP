package com.retro99.auth.domain.usecase

import com.github.michaelbull.result.Ok
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseCleaner
import com.retro99.server.api.ServerRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class LogoutUseCase(
    @Provided private val serverRegistry: ServerRegistry,
    @Provided private val databaseCleaner: DatabaseCleaner,
) {
    /**
     * Logout from a specific server.
     * @param serverId The server to logout from.
     */
    suspend operator fun invoke(serverId: String): CompletableResult {
        serverRegistry.clearCredentials(serverId)
        // TODO: Clear only data for this server when supported
        // databaseCleaner.clearDataForServer(serverId)
        return Ok(Unit)
    }

    /**
     * Logout from all servers.
     */
    suspend fun logoutAll(): CompletableResult {
        serverRegistry.clearAllCredentials()
        databaseCleaner.clearAllData()
        return Ok(Unit)
    }
}

