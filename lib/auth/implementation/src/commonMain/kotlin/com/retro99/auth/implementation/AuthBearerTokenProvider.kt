package com.retro99.auth.implementation

import com.retro99.auth.api.AuthRepository
import com.retro99.auth.api.tokens.BearerTokenProvider
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [BearerTokenProvider::class])
class AuthBearerTokenProvider(
    @Provided private val authRepository: AuthRepository,
) : BearerTokenProvider {

    override suspend fun getBearerToken(): String? {
        return authRepository.getCredentials()?.token
    }
}

