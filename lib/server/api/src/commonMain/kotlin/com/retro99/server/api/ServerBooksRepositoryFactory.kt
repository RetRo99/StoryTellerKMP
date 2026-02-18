package com.retro99.server.api

/**
 * Factory for creating server-specific BooksRepository implementations.
 */
interface ServerBooksRepositoryFactory {
    /**
     * Create a BooksRepository for a specific server.
     */
    fun create(serverConfig: ServerConfig): ServerBooksRepository

    /**
     * Create repositories for all authenticated servers.
     */
    suspend fun createForAuthenticatedServers(): List<ServerBooksRepository>

    /**
     * Invalidate cached repository for a server.
     * Call this when server credentials change or server is removed.
     */
    fun invalidateCache(serverId: String)

    /**
     * Invalidate all cached repositories.
     */
    fun invalidateAllCaches()
}

