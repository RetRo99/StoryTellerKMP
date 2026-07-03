package com.retro99.reader.domain.usecase

import com.github.michaelbull.result.Err
import com.retro99.base.nowMillis
import com.retro99.base.result.AppError
import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.model.PositionDomainModel
import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.ServerPosition
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for saving reading progress.
 * Saves to local cache first, then syncs to remote server.
 *
 * Follows the Books pattern: uses AuthenticatedRepositoryProvider directly
 * to get ServerReaderRepository, which owns both local and remote position data.
 */
@Factory
class SaveReadingProgressUseCase(
    @Provided private val repositoryProvider: AuthenticatedRepositoryProvider,
) {
    suspend operator fun invoke(progress: PositionDomainModel): CompletableResult {
        val serverRepository = repositoryProvider.getReaderRepository(progress.serverId)
            ?: return Err(AppError.NotFoundError("Server not found: ${progress.serverId}"))

        return serverRepository.savePosition(
            bookUuid = progress.bookUuid,
            position = progress.toServerPosition(),
        )
    }
}

/**
 * Converts a PositionDomainModel to ServerPosition.
 * Always uses current timestamp to ensure the server accepts the position.
 */
private fun PositionDomainModel.toServerPosition(): ServerPosition {
    return ServerPosition(
        bookUuid = bookUuid,
        serverId = serverId,
        timestamp = nowMillis(),
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
        cssSelector = cssSelector,
    )
}

