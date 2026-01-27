package com.retro99.auth.data

import com.retro99.auth.data.model.CredentialsLocalModel
import com.retro99.auth.data.model.toDomain
import com.retro99.auth.data.model.toLocal
import com.retro99.auth.domain.AuthRepository
import com.retro99.auth.domain.model.CredentialsDomainModel
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.preferences.api.putObject
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class])
internal class AuthDataRepository(
    @Provided private val preferences: Preferences,
) : AuthRepository {

    override suspend fun saveCredentials(credentials: CredentialsDomainModel) {
        preferences.putObject(PreferencesKey.Credentials, credentials.toLocal())
    }

    override suspend fun getCredentials(): CredentialsDomainModel? {
        return preferences.getObject<CredentialsLocalModel>(PreferencesKey.Credentials)?.toDomain()
    }

    override suspend fun isLoggedIn(): Boolean {
        return getCredentials() != null
    }

    override suspend fun clearCredentials() {
        preferences.remove(PreferencesKey.Credentials)
    }
}

