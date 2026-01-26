package com.retro99.login.data

import org.koin.core.annotation.Single
import retro99.network.api.tokens.BearerTokenRefresher

@Single(binds = [BearerTokenRefresher::class])
class LoginBearerTokenRefresher : BearerTokenRefresher {

    override suspend fun refreshBearerToken(): String? {
        return null
    }
}

