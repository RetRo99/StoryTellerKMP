package com.retro99.auth.domain.usecase

import com.retro99.server.api.ServerRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class CheckAuthStateUseCase(
    @Provided private val serverRegistry: ServerRegistry,
) {
    suspend operator fun invoke(): Boolean {
        return serverRegistry.getAuthenticatedServers().isNotEmpty()
    }
}

