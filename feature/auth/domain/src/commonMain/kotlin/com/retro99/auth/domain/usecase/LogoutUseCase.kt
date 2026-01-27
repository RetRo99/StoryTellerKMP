package com.retro99.auth.domain.usecase

import com.github.michaelbull.result.Ok
import com.retro99.auth.domain.AuthRepository
import com.retro99.base.result.CompletableResult
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.BaseUrlProvider

@Factory
class LogoutUseCase(
    @Provided private val authRepository: AuthRepository,
    @Provided private val baseUrlProvider: BaseUrlProvider,
) {
    suspend operator fun invoke(): CompletableResult {
        authRepository.clearCredentials()
        baseUrlProvider.clearBaseUrl()
        return Ok(Unit)
    }
}

