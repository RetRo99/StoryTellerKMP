package com.retro99.auth.domain.usecase

import com.github.michaelbull.result.Ok
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseCleaner
import com.retro99.server.api.ServerRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.BaseUrlProvider

@Factory
class LogoutUseCase(
    @Provided private val serverRegistry: ServerRegistry,
    @Provided private val baseUrlProvider: BaseUrlProvider,
    @Provided private val databaseCleaner: DatabaseCleaner,
) {
    suspend operator fun invoke(): CompletableResult {
        serverRegistry.clearAllCredentials()
        baseUrlProvider.clearBaseUrl()
        databaseCleaner.clearAllData()
        return Ok(Unit)
    }
}

