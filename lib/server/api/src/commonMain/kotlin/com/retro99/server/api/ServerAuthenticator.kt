package com.retro99.server.api

import com.github.michaelbull.result.Err
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult

/**
 * Handles authentication for a specific server type.
 */
interface ServerAuthenticator {
    /**
     * The server type this authenticator handles.
     */
    val serverType: ServerType

    /**
     * Authenticate with username and password.
     */
    suspend fun login(
        baseUrl: String,
        username: String,
        password: String,
    ): AppResult<ServerCredentials>

    /**
     * Refresh an expired token.
     */
    suspend fun refreshToken(
        baseUrl: String,
        refreshToken: String,
    ): AppResult<ServerCredentials>

    /**
     * Exchange a short-lived mobile app OAuth token for API credentials.
     */
    suspend fun loginWithAppToken(
        baseUrl: String,
        appToken: String,
    ): AppResult<ServerCredentials> {
        return Err(AppError.AuthError("OAuth app token login not supported for $serverType"))
    }

    /**
     * Validate that a server URL is reachable and correct type.
     */
    suspend fun validateServer(baseUrl: String): AppResult<ServerValidationResult>
}

/**
 * Result of validating a server URL.
 */
data class ServerValidationResult(
    val isValid: Boolean,
    val serverVersion: String?,
    val serverName: String?,
    val errorMessage: String?,
)

