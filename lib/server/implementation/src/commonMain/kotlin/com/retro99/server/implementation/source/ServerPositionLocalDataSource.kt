package com.retro99.server.implementation.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.PositionDatabase
import com.retro99.database.api.books.PositionEntity
import com.retro99.server.api.ServerPosition
import com.retro99.server.api.ServerPositionLocalSource
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Implementation of ServerPositionLocalSource that uses PositionDatabase for storage.
 * This is shared by all server implementations (Storyteller, Local, etc.).
 */
@Single(binds = [ServerPositionLocalSource::class])
class ServerPositionLocalDataSource(
    @Provided private val positionDatabase: PositionDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : ServerPositionLocalSource {

    override suspend fun getPosition(bookUuid: String): AppResult<ServerPosition?> {
        return databaseExecutor.executeDatabaseOperation {
            positionDatabase.getPositionByBookUuid(bookUuid)?.toServerPosition()
        }
    }

    override suspend fun savePosition(position: ServerPosition): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            positionDatabase.upsertPosition(position.toPositionEntity())
        }
    }

    override suspend fun getAllPositions(): AppResult<List<ServerPosition>> {
        return databaseExecutor.executeDatabaseOperation {
            positionDatabase.getAllPositions().map { it.toServerPosition() }
        }
    }

    override suspend fun deletePosition(bookUuid: String): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            positionDatabase.deletePosition(bookUuid)
        }
    }
}

/**
 * Converts a PositionEntity to ServerPosition.
 * Note: serverId is not stored in the database, so we use an empty string.
 * The actual serverId should be set by the caller based on context.
 */
private fun PositionEntity.toServerPosition(): ServerPosition {
    return ServerPosition(
        bookUuid = bookUuid,
        serverId = "", // Not stored in DB - will be set by repository
        timestamp = timestamp,
        createdAt = createdAt,
        updatedAt = updatedAt,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        locatorTarget = locatorTarget,
        audioTimestampMs = audioTimestampMs,
        chapterIndex = chapterIndex,
        progression = progression,
        totalChapters = totalChapters,
        totalDurationMs = totalDurationMs,
        totalProgression = totalProgression,
        position = position,
    )
}

/**
 * Converts a ServerPosition to a PositionEntity for database storage.
 */
private fun ServerPosition.toPositionEntity(): PositionEntity {
    return ServerPositionEntity(
        bookUuid = bookUuid,
        timestamp = timestamp,
        createdAt = createdAt,
        updatedAt = updatedAt,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        locatorTarget = locatorTarget,
        audioTimestampMs = audioTimestampMs,
        chapterIndex = chapterIndex,
        progression = progression,
        totalChapters = totalChapters,
        totalDurationMs = totalDurationMs,
        totalProgression = totalProgression,
        position = position,
    )
}

/**
 * Internal PositionEntity implementation for database storage.
 */
private data class ServerPositionEntity(
    override val bookUuid: String,
    override val timestamp: Long?,
    override val createdAt: String?,
    override val updatedAt: String?,
    override val locatorHref: String?,
    override val locatorType: String?,
    override val locatorTitle: String?,
    override val locatorTarget: Int?,
    override val audioTimestampMs: Long?,
    override val chapterIndex: Int?,
    override val progression: Double?,
    override val totalChapters: Int?,
    override val totalDurationMs: Long?,
    override val totalProgression: Double?,
    override val position: Int?,
) : PositionEntity

