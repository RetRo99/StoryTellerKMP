package com.retro99.login.domain

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.login.domain.model.LoginCredentialsDomainModel

interface LoginRepository {

    suspend fun login(
        serverUrl: String,
        username: String,
        password: String,
    ): CompletableResult

    suspend fun getStoredCredentials(): AppResult<LoginCredentialsDomainModel?>

    suspend fun isLoggedIn(): Boolean

    suspend fun logout(): CompletableResult
}
