package com.retro99.auth.implementation

import com.retro99.auth.api.tokens.BearerTokenRefresher
import org.koin.core.annotation.Single

@Single(binds = [BearerTokenRefresher::class])
class AuthBearerTokenRefresher : BearerTokenRefresher {

    override suspend fun refreshBearerToken(): String? {
        return null
    }
}

