package com.retro99.auth.domain.usecase

import com.retro99.auth.domain.AuthRepository
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

