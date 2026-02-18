package com.retro99.server.api

import kotlinx.coroutines.flow.Flow

/**
 * Provides repositories only for authenticated servers.
 * Use cases depend on this to get the correct repositories.
 */
interface AuthenticatedRepositoryProvider {
    /**
     * Observe repositories for all authenticated servers.
     * Automatically updates when servers are added/removed or auth state changes.
     */
    fun observeBooksRepositories(): Flow<List<ServerBooksRepository>>

    /**
     * Get repositories for all authenticated servers (suspend version).
     */
    suspend fun getBooksRepositories(): List<ServerBooksRepository>

    /**
     * Get repository for a specific server.
     * @return null if server doesn't exist or is not authenticated
     */
    suspend fun getBooksRepository(serverId: String): ServerBooksRepository?

    /**
     * Get repository for the active server.
     * @return null if no active server or not authenticated
     */
    suspend fun getActiveBooksRepository(): ServerBooksRepository?
}

