package com.retro99.login.data

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.onFailure
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.CompletableResult
import com.retro99.base.server.ServerType
import com.retro99.login.domain.LoginRepository
import com.retro99.server.api.ServerAuthenticatorFactory
import com.retro99.server.api.ServerRegistry
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [LoginRepository::class])
internal class LoginDataRepository(
    @Provided private val authenticatorFactory: ServerAuthenticatorFactory,
    @Provided private val analytics: Analytics,
    @Provided private val serverRegistry: ServerRegistry,
) : LoginRepository {

    override suspend fun login(
        serverType: ServerType,
        serverUrl: String,
        username: String,
        password: String,
    ): CompletableResult {
        val authenticator = authenticatorFactory.create(serverType)

        return authenticator.login(serverUrl, username, password)
            .onFailure { error ->
                // Never log serverUrl for privacy - only log error type
                analytics.logException(
                    error.toThrowable(),
                    "LoginRepository: Login failed | errorType=${error::class.simpleName}"
                )
            }
            .flatMap { credentials ->
                // Register server in ServerRegistry
                val serverConfig = serverRegistry.addServer(
                    name = serverType.displayName,
                    type = serverType,
                    baseUrl = serverUrl,
                )
                serverRegistry.saveCredentials(credentials.copy(serverId = serverConfig.id))
                serverRegistry.setActiveServer(serverConfig.id)

                Ok(Unit)
            }
    }
}

