package com.retro99.login.data

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.onFailure
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.login.data.model.LoginRequestApiModel
import com.retro99.login.data.source.CredentialsLocalSource
import com.retro99.login.data.source.LoginRemoteSource
import com.retro99.login.domain.LoginRepository
import com.retro99.login.domain.model.LoginCredentialsDomainModel
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [LoginRepository::class])
internal class LoginDataRepository(
    @Provided private val remoteSource: LoginRemoteSource,
    @Provided private val localSource: CredentialsLocalSource,
    @Provided private val preferences: Preferences,
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
            .onFailure {
                preferences.remove(PreferencesKey.ServerUrl)
            }
            .flatMap { tokenResponse ->
                val credentials = LoginCredentialsDomainModel(
                    username = username,
                    serverUrl = serverUrl,
                    token = tokenResponse.accessToken,
                )
                localSource.saveCredentials(credentials)

                Ok(Unit)
            }
    }

    override suspend fun getStoredCredentials(): AppResult<LoginCredentialsDomainModel?> {
        return Ok(localSource.getCredentials())
    }

    override suspend fun isLoggedIn(): Boolean {
        return localSource.isLoggedIn()
    }

    override suspend fun logout(): CompletableResult {
        localSource.clearCredentials()
        return Ok(Unit)
    }
}

