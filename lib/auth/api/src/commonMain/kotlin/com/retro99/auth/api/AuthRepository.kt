package com.retro99.auth.api

import com.retro99.auth.api.model.CredentialsDomainModel

interface AuthRepository {

    suspend fun saveCredentials(credentials: CredentialsDomainModel)

    suspend fun getCredentials(): CredentialsDomainModel?

    suspend fun isLoggedIn(): Boolean

    suspend fun clearCredentials()
}

