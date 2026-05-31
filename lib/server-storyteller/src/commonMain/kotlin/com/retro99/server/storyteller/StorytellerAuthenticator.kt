package com.retro99.server.storyteller

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.server.api.ServerAuthenticator
import com.retro99.server.api.ServerCredentials
import com.retro99.server.api.ServerType
import com.retro99.server.api.ServerValidationResult
import com.retro99.server.storyteller.model.StorytellerAppTokenRequest
import com.retro99.server.storyteller.model.StorytellerCurrentUserResponse
import com.retro99.server.storyteller.model.StorytellerTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.time.Clock
import org.koin.core.annotation.Factory

@Factory
class StorytellerAuthenticator(
    private val httpClient: HttpClient,
) : ServerAuthenticator {

    override val serverType: ServerType = ServerType.Storyteller

    override suspend fun login(
        baseUrl: String,
        username: String,
        password: String,
    ): AppResult<ServerCredentials> {
        return try {
            val response = httpClient.submitForm(
                url = "${baseUrl.trimEnd('/')}/api/v2/token",
                formParameters = Parameters.build {
                    append("usernameOrEmail", username)
                    append("password", password)
                }
            )

            if (response.status.isSuccess()) {
                val tokenResponse = response.body<StorytellerTokenResponse>()
                val now = Clock.System.now().toEpochMilliseconds()
                val expiresAt = tokenResponse.expiresIn?.let { now + (it * 1000) }

                Ok(
                    ServerCredentials(
                        serverId = "", // Will be set by caller
                        username = username,
                        accessToken = tokenResponse.accessToken,
                        refreshToken = null, // Storyteller doesn't use refresh tokens currently
                        expiresAt = expiresAt,
                    )
                )
            } else {
                Err(AppError.AuthError("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Err(mapException(e))
        }
    }

    override suspend fun refreshToken(
        baseUrl: String,
        refreshToken: String,
    ): AppResult<ServerCredentials> {
        // Storyteller doesn't support token refresh currently
        return Err(AppError.AuthError("Token refresh not supported for Storyteller"))
    }

    override suspend fun loginWithAppToken(
        baseUrl: String,
        appToken: String,
    ): AppResult<ServerCredentials> {
        return try {
            val tokenResponse = httpClient.post("${baseUrl.trimEnd('/')}/api/v2/token/app") {
                contentType(ContentType.Application.Json)
                setBody(StorytellerAppTokenRequest(appToken))
            }

            if (!tokenResponse.status.isSuccess()) {
                return Err(AppError.AuthError("OAuth login failed: ${tokenResponse.status}"))
            }

            val sessionToken = tokenResponse.body<StorytellerTokenResponse>()
            val userResponse = httpClient.get("${baseUrl.trimEnd('/')}/api/v2/user") {
                bearerAuth(sessionToken.accessToken)
            }

            if (!userResponse.status.isSuccess()) {
                return Err(AppError.AuthError("OAuth login succeeded, but user lookup failed: ${userResponse.status}"))
            }

            val currentUser = userResponse.body<StorytellerCurrentUserResponse>()
            val username = currentUser.username
                ?: return Err(AppError.AuthError("OAuth login succeeded, but the server did not return a username"))

            Ok(
                ServerCredentials(
                    serverId = "", // Will be set by caller
                    username = username,
                    accessToken = sessionToken.accessToken,
                    refreshToken = null,
                    expiresAt = null,
                )
            )
        } catch (e: Exception) {
            Err(mapException(e))
        }
    }

    override suspend fun validateServer(baseUrl: String): AppResult<ServerValidationResult> {
        return try {
            val response = httpClient.get("${baseUrl.trimEnd('/')}/api/v2/info")

            if (response.status.isSuccess()) {
                Ok(
                    ServerValidationResult(
                        isValid = true,
                        serverVersion = null, // Could parse from response if available
                        serverName = "Storyteller",
                        errorMessage = null,
                    )
                )
            } else {
                Ok(
                    ServerValidationResult(
                        isValid = false,
                        serverVersion = null,
                        serverName = null,
                        errorMessage = "Server returned ${response.status}",
                    )
                )
            }
        } catch (e: Exception) {
            Ok(
                ServerValidationResult(
                    isValid = false,
                    serverVersion = null,
                    serverName = null,
                    errorMessage = e.message,
                )
            )
        }
    }

    private fun mapException(e: Exception): AppError {
        return when {
            e.message?.contains("401") == true -> AppError.AuthError("Invalid credentials")
            e.message?.contains("timeout", ignoreCase = true) == true -> AppError.NetworkError(e)
            else -> AppError.UnknownError(e)
        }
    }
}

