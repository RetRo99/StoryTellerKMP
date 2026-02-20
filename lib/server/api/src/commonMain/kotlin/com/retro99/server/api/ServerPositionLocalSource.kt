package com.retro99.server.api

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult

/**
 * Local source interface for position storage.
 * Used by ServerReaderRepository implementations to cache positions locally.
 */
interface ServerPositionLocalSource {

    /**
     * Gets the locally cached position for a book.
     *
     * @param bookUuid The UUID of the book
     * @return The cached position or null if not found
     */
    suspend fun getPosition(bookUuid: String): AppResult<ServerPosition?>

    /**
     * Saves a position to the local cache.
     *
     * @param position The position to save
     */
    suspend fun savePosition(position: ServerPosition): CompletableResult

    /**
     * Gets all locally cached positions.
     *
     * @return List of all cached positions
     */
    suspend fun getAllPositions(): AppResult<List<ServerPosition>>

    /**
     * Deletes a position from the local cache.
     *
     * @param bookUuid The UUID of the book
     */
    suspend fun deletePosition(bookUuid: String): CompletableResult
}

