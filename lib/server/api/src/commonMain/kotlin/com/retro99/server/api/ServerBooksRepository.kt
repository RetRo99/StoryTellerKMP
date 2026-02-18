package com.retro99.server.api

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import kotlinx.coroutines.flow.Flow

/**
 * Server-aware books repository interface.
 * Extends the base repository pattern with server context.
 */
interface ServerBooksRepository {
    /**
     * The server ID this repository is associated with.
     */
    val serverId: String

    /**
     * Get all books from this server.
     */
    fun getBooks(): Flow<AppResult<List<ServerBook>>>

    /**
     * Get a specific book by UUID.
     */
    fun getBook(uuid: String): Flow<AppResult<ServerBook>>

    /**
     * Save a book to this server.
     */
    suspend fun saveBook(book: ServerBook): CompletableResult

    /**
     * Search for books on this server.
     */
    suspend fun searchBooks(query: String): AppResult<List<ServerBook>>
}

/**
 * A book associated with a specific server.
 * This is a lightweight wrapper that includes server context.
 */
data class ServerBook(
    val uuid: String,
    val serverId: String,
    val title: String,
    val description: String?,
    val coverUrl: String?,
    val authors: List<String>,
    val narrators: List<String>,
    val series: List<ServerBookSeries>,
    val hasEbook: Boolean,
    val hasAudiobook: Boolean,
    val metadata: Map<String, Any?> = emptyMap(),
)

data class ServerBookSeries(
    val id: String?,
    val name: String,
    val sequence: Float?,
)

