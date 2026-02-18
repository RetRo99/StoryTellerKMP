package com.retro99.server.api

/**
 * Provides authentication tokens for specific servers.
 */
interface ServerTokenProvider {
    /**
     * Get the bearer token for a specific server.
     * @return Token string or null if not authenticated
     */
    suspend fun getToken(serverId: String): String?

    /**
     * Get the token for the currently active server.
     */
    suspend fun getActiveServerToken(): String?

    /**
     * Refresh the token for a specific server.
     * @return New token or null if refresh failed
     */
    suspend fun refreshToken(serverId: String): String?
}

