package com.retro99.reader.domain.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppResult
import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.PositionDomainModel
import com.retro99.reader.domain.model.ReadingProgressResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import kotlin.math.abs

/**
 * Use case for getting reading progress with conflict detection.
 *
 * This use case fetches both local and remote positions and detects
 * if there's a conflict that requires user intervention.
 */
@Factory
class GetReadingProgressWithConflictUseCase(
    @Provided private val readerRepository: ReaderRepository,
) {
    /**
     * Gets the reading progress for a book with conflict detection.
     *
     * @param serverId The ID of the server the book belongs to
     * @param bookUuid The UUID of the book
     * @return [ReadingProgressResult.Resolved] if no conflict or positions are the same,
     *         [ReadingProgressResult.Conflict] if user needs to choose between positions
     */
    suspend operator fun invoke(serverId: String, bookUuid: String): AppResult<ReadingProgressResult> =
        coroutineScope {
            val localDeferred = async {
                readerRepository.getLocalReadingProgress(serverId, bookUuid)
            }
            val remoteDeferred = async {
                readerRepository.getRemoteReadingProgress(serverId, bookUuid)
            }

            val localPosition = localDeferred.await().getOrElse { null }
            val remotePosition = remoteDeferred.await().getOrElse { null }

            Ok(resolvePositionConflict(localPosition, remotePosition))
        }

    /**
     * Resolves potential conflicts between local and remote positions.
     *
     * Conflict is detected when both positions exist and have different progression values.
     * If no conflict, returns the most appropriate position (preferring remote if available).
     */
    private fun resolvePositionConflict(
        localPosition: PositionDomainModel?,
        remotePosition: PositionDomainModel?,
    ): ReadingProgressResult {
        return when {
            // Both null - no position exists
            localPosition == null && remotePosition == null -> {
                ReadingProgressResult.Resolved(null)
            }
            // Only remote exists
            localPosition == null -> {
                ReadingProgressResult.Resolved(remotePosition)
            }
            // Only local exists
            remotePosition == null -> {
                ReadingProgressResult.Resolved(localPosition)
            }
            // Both exist - check for conflict
            hasProgressionConflict(localPosition, remotePosition) -> {
                ReadingProgressResult.Conflict(
                    localPosition = localPosition,
                    remotePosition = remotePosition,
                )
            }
            // No significant difference - prefer remote as source of truth
            else -> {
                ReadingProgressResult.Resolved(remotePosition)
            }
        }
    }

    private fun hasProgressionConflict(
        localPosition: PositionDomainModel,
        remotePosition: PositionDomainModel,
    ): Boolean {
        val localProgression =
            localPosition.totalProgression ?: localPosition.progression ?: 0.0
        val remoteProgression =
            remotePosition.totalProgression ?: remotePosition.progression ?: 0.0

        val progressionDifference = abs(localProgression - remoteProgression)
        return progressionDifference > PROGRESSION_CONFLICT_THRESHOLD
    }

    companion object {
        /**
         * Minimum difference in progression (0.0 to 1.0) to consider as a conflict.
         * 1% difference threshold to avoid false positives from floating point issues.
         */
        private const val PROGRESSION_CONFLICT_THRESHOLD = 0.01
    }
}

