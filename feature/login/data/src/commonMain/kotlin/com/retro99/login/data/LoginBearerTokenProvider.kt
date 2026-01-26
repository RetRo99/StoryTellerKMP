package com.retro99.login.data

import com.retro99.login.data.source.CredentialsLocalSource
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.tokens.BearerTokenProvider

@Single(binds = [BearerTokenProvider::class])
class LoginBearerTokenProvider(
    @Provided private val credentialsLocalSource: CredentialsLocalSource,
) : BearerTokenProvider {

    override suspend fun getBearerToken(): String? {
        return credentialsLocalSource.getCredentials()?.token
    }
}

