package com.retro99.login.domain.usecase

import com.retro99.login.domain.LoginRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class CheckAuthStateUseCase(
    @Provided private val loginRepository: LoginRepository,
) {
    suspend operator fun invoke(): Boolean {
        return loginRepository.isLoggedIn()
    }
}

