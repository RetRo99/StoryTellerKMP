package com.retro99.server.api

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult

/**
 * Server-aware reader repository interface for reading progress sync.
 * Each server type implements this with its own API.
 */
interface ServerReaderRepository {
    /**
     * The server ID this repository is associated with.
     */
    val serverId: String

    /**
     * Gets the reading position for a book from the server.
     *
     * @param bookUuid The UUID of the book
     * @return The position from the server, or null if not found
     */
    suspend fun getPosition(bookUuid: String): AppResult<ServerPosition?>

    /**
     * Updates the reading position for a book on the server.
     *
     * @param bookUuid The UUID of the book
     * @param position The position to save
     */
    suspend fun updatePosition(bookUuid: String, position: ServerPosition): CompletableResult
}

/**
 * A reading position associated with a specific server.
 * This is a server-agnostic representation that each server implementation
 * converts to/from its own API format.
 */
data class ServerPosition(
    val bookUuid: String,
    val serverId: String,
    val timestamp: Long?,
    val createdAt: String?,
    val updatedAt: String?,
    // Locator fields
    val locatorHref: String?,
    val locatorType: String?,
    val locatorTitle: String?,
    val locatorTarget: Int?,
    // Location fields
    val audioTimestampMs: Long?,
    val chapterIndex: Int?,
    val progression: Double?,
    val totalChapters: Int?,
    val totalDurationMs: Long?,
    val totalProgression: Double?,
    val position: Int?,
)

