package com.retro99.server.api

/**
 * Represents the authentication state for a specific server.
 */
sealed class ServerAuthState {
    abstract val serverId: String

    data class Authenticated(
        override val serverId: String,
        val username: String,
        val authenticatedAt: Long,
    ) : ServerAuthState()

    data class NotAuthenticated(
        override val serverId: String,
    ) : ServerAuthState()

    data class AuthenticationFailed(
        override val serverId: String,
        val error: AuthError,
        val failedAt: Long,
    ) : ServerAuthState()

    data class TokenExpired(
        override val serverId: String,
        val expiredAt: Long,
    ) : ServerAuthState()
}

sealed class AuthError {
    data object InvalidCredentials : AuthError()
    data object NetworkError : AuthError()
    data object ServerUnreachable : AuthError()
    data object TokenRefreshFailed : AuthError()
    data class Unknown(val message: String?) : AuthError()
}

