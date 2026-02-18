package com.retro99.server.storyteller.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.server.api.ServerBook

/**
 * Local data source for server books.
 * Caches books per-server to avoid conflicts between different servers.
 */
interface ServerBooksLocalSource {

    /**
     * Get all cached books for a specific server.
     * @return null if no cached books exist for this server
     */
    suspend fun getBooks(serverId: String): AppResult<List<ServerBook>?>

    /**
     * Get a specific cached book by UUID for a server.
     * @return null if the book is not cached
     */
    suspend fun getBook(serverId: String, uuid: String): AppResult<ServerBook?>

    /**
     * Save books to cache for a specific server.
     */
    suspend fun saveBooks(serverId: String, books: List<ServerBook>): CompletableResult

    /**
     * Save a single book to cache for a specific server.
     */
    suspend fun saveBook(serverId: String, book: ServerBook): CompletableResult

    /**
     * Clear all cached books for a specific server.
     */
    suspend fun clearCache(serverId: String): CompletableResult
}

