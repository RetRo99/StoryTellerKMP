package com.retro99.login.data.source

import com.retro99.login.data.model.CredentialsLocalModel
import com.retro99.login.data.model.toDomain
import com.retro99.login.data.model.toLocal
import com.retro99.login.domain.model.LoginCredentialsDomainModel
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.preferences.api.putObject
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
internal class CredentialsPreferencesDataSource(
    @Provided private val preferences: Preferences,
) : CredentialsLocalSource {

    override suspend fun saveCredentials(credentials: LoginCredentialsDomainModel) {
        preferences.putObject(PreferencesKey.Credentials, credentials.toLocal())
    }

    override suspend fun getCredentials(): LoginCredentialsDomainModel? {
        return preferences.getObject<CredentialsLocalModel>(PreferencesKey.Credentials)?.toDomain()
    }

    override suspend fun isLoggedIn(): Boolean {
        return getCredentials() != null
    }

    override suspend fun clearCredentials() {
        preferences.remove(PreferencesKey.Credentials)
    }
}

