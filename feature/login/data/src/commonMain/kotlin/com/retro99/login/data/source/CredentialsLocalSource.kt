package com.retro99.login.data.source

import com.retro99.login.domain.model.LoginCredentialsDomainModel

interface CredentialsLocalSource {

    suspend fun saveCredentials(credentials: LoginCredentialsDomainModel)

    suspend fun getCredentials(): LoginCredentialsDomainModel?

    suspend fun isLoggedIn(): Boolean

    suspend fun clearCredentials()
}

