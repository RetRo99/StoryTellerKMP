package com.retro99.database.api.books

/**
 * Database interface for reading position operations.
 */
interface PositionDatabase {

    suspend fun upsertPosition(position: PositionEntity)

    suspend fun getPositionByBookUuid(bookUuid: String): PositionEntity?

    suspend fun deletePosition(bookUuid: String)
}
