package com.retro99.auth.data

import com.retro99.auth.domain.tokens.BearerTokenProvider
import com.retro99.server.api.ServerTokenProvider
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [BearerTokenProvider::class])
class AuthBearerTokenProvider(
    @Provided private val serverTokenProvider: ServerTokenProvider,
) : BearerTokenProvider {

    override suspend fun getBearerToken(): String? {
        return serverTokenProvider.getActiveServerToken()
    }
}

