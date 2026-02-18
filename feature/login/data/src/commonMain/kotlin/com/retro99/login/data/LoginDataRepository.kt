package com.retro99.login.data

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.onFailure
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.CompletableResult
import com.retro99.login.data.model.LoginRequestApiModel
import com.retro99.login.data.source.LoginRemoteSource
import com.retro99.login.domain.LoginRepository
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.server.api.ServerCredentials
import com.retro99.server.api.ServerRegistry
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [LoginRepository::class])
internal class LoginDataRepository(
    @Provided private val remoteSource: LoginRemoteSource,
    @Provided private val preferences: Preferences,
    @Provided private val analytics: Analytics,
    @Provided private val serverRegistry: ServerRegistry,
) : LoginRepository {

    override suspend fun login(
        serverUrl: String,
        username: String,
        password: String,
    ): CompletableResult {

        preferences.putString(PreferencesKey.ServerUrl, serverUrl)

        val request = LoginRequestApiModel(
            usernameOrEmail = username,
            password = password,
        )

        return remoteSource.login(request)
            .onFailure { error ->
                // Never log serverUrl for privacy - only log error type
                analytics.logException(
                    error.toThrowable(),
                    "LoginRepository: Login failed | errorType=${error::class.simpleName}"
                )
                preferences.remove(PreferencesKey.ServerUrl)
            }
            .flatMap { tokenResponse ->
                // Register server in ServerRegistry
                val serverConfig = serverRegistry.addServer(
                    name = "Storyteller",
                    type = ServerType.Storyteller,
                    baseUrl = serverUrl,
                )
                serverRegistry.saveCredentials(
                    ServerCredentials(
                        serverId = serverConfig.id,
                        username = username,
                        accessToken = tokenResponse.accessToken,
                        refreshToken = null,
                        expiresAt = null,
                    )
                )
                serverRegistry.setActiveServer(serverConfig.id)

                Ok(Unit)
            }
    }
}

