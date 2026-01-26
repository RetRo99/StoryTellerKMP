package com.retro99.login.domain.usecase

import com.github.michaelbull.result.onSuccess
import com.retro99.base.result.CompletableResult
import com.retro99.login.domain.LoginRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.BaseUrlProvider

@Factory
class LogoutUseCase(
    @Provided private val loginRepository: LoginRepository,
    @Provided private val baseUrlProvider: BaseUrlProvider,
) {
    suspend operator fun invoke(): CompletableResult {
        return loginRepository.logout()
            .onSuccess {
                baseUrlProvider.clearBaseUrl()
            }
    }
}

