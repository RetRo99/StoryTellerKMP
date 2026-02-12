package com.retro99.login.data

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.onFailure
import com.retro99.analytics.api.Analytics
import com.retro99.auth.domain.AuthRepository
import com.retro99.auth.domain.model.CredentialsDomainModel
import com.retro99.base.result.CompletableResult
import com.retro99.login.data.model.LoginRequestApiModel
import com.retro99.login.data.source.LoginRemoteSource
import com.retro99.login.domain.LoginRepository
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [LoginRepository::class])
internal class LoginDataRepository(
    @Provided private val remoteSource: LoginRemoteSource,
    @Provided private val authRepository: AuthRepository,
    @Provided private val preferences: Preferences,
    @Provided private val analytics: Analytics,
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
                analytics.logException(
                    error.toThrowable(),
                    "LoginRepository: Login failed for server=$serverUrl"
                )
                preferences.remove(PreferencesKey.ServerUrl)
            }
            .flatMap { tokenResponse ->
                val credentials = CredentialsDomainModel(
                    username = username,
                    serverUrl = serverUrl,
                    token = tokenResponse.accessToken,
                )
                authRepository.saveCredentials(credentials)

                Ok(Unit)
            }
    }
}

