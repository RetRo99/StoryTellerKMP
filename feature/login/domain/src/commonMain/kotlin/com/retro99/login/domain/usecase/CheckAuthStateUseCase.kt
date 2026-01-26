package com.retro99.login.domain.usecase

import com.retro99.auth.api.AuthRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class CheckAuthStateUseCase(
    @Provided private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
}

