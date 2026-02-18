package com.retro99.auth.domain.usecase

import com.retro99.base.server.ServerType
import com.retro99.server.api.ServerRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class CheckAuthStateUseCase(
    @Provided private val serverRegistry: ServerRegistry,
) {
    /**
     * Returns true if the user is logged into at least one remote server.
     * Local server is excluded since it doesn't require authentication.
     */
    suspend operator fun invoke(): Boolean {
        return serverRegistry.getAuthenticatedServers()
            .any { it.type != ServerType.Local }
    }
}

