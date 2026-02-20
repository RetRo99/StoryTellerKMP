package com.retro99.database.api.books

import com.retro99.database.api.DataClearable

/**
 * Database interface for reading position operations.
 */
interface PositionDatabase : DataClearable {

    suspend fun upsertPosition(position: PositionEntity)

    suspend fun getPositionByBookUuid(bookUuid: String): PositionEntity?

    suspend fun getAllPositions(): List<PositionEntity>

    suspend fun deletePosition(bookUuid: String)
}
