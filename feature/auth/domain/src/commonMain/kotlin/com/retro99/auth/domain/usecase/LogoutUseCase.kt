package com.retro99.auth.domain.usecase

import com.github.michaelbull.result.Ok
import com.retro99.auth.domain.AuthRepository
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseCleaner
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.BaseUrlProvider

@Factory
class LogoutUseCase(
    @Provided private val authRepository: AuthRepository,
    @Provided private val baseUrlProvider: BaseUrlProvider,
    @Provided private val databaseCleaner: DatabaseCleaner,
) {
    suspend operator fun invoke(): CompletableResult {
        authRepository.clearCredentials()
        baseUrlProvider.clearBaseUrl()
        databaseCleaner.clearAllData()
        return Ok(Unit)
    }
}

