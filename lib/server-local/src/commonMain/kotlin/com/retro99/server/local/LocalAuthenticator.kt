package com.retro99.server.local

import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppResult
import com.retro99.base.server.LOCAL_SERVER_ID
import com.retro99.server.api.ServerAuthenticator
import com.retro99.server.api.ServerCredentials
import com.retro99.server.api.ServerType
import com.retro99.server.api.ServerValidationResult
import org.koin.core.annotation.Single

/**
 * Local server authenticator - no authentication required.
 * Local files are always accessible without credentials.
 */
@Single
class LocalAuthenticator : ServerAuthenticator {

    override val serverType: ServerType = ServerType.Local

    override suspend fun login(
        baseUrl: String,
        username: String,
        password: String,
    ): AppResult<ServerCredentials> {
        // Local server doesn't require authentication
        // Return dummy credentials that never expire
        return Ok(
            ServerCredentials(
                serverId = LOCAL_SERVER_ID,
                username = "local",
                accessToken = "local-access",
                refreshToken = null,
                expiresAt = null,
            )
        )
    }

    override suspend fun refreshToken(
        baseUrl: String,
        refreshToken: String,
    ): AppResult<ServerCredentials> {
        // Local server doesn't need token refresh
        return login(baseUrl, "", "")
    }

    override suspend fun validateServer(baseUrl: String): AppResult<ServerValidationResult> {
        // Local server is always valid
        return Ok(
            ServerValidationResult(
                isValid = true,
                serverVersion = "1.0",
                serverName = LOCAL_SERVER_NAME,
                errorMessage = null,
            )
        )
    }

    companion object {
        const val LOCAL_SERVER_NAME = "Local Files"
    }
}

