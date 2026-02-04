package com.retro99.reader.domain.model

import com.retro99.books.domain.model.PositionDomainModel

/**
 * Represents the result of fetching reading progress with conflict detection.
 *
 * When both local and remote positions exist and differ significantly,
 * a [Conflict] is returned to allow the user to choose which position to use.
 */
sealed class ReadingProgressResult {

    /**
     * No conflict detected. Either:
     * - Only one source has a position
     * - Both sources have the same position
     * - One position is clearly newer/further and was auto-selected
     *
     * @param position The resolved position, or null if no position exists
     */
    data class Resolved(
        val position: PositionDomainModel?,
    ) : ReadingProgressResult()

    /**
     * A conflict was detected between local and remote positions.
     * The user should be prompted to choose which position to use.
     *
     * @param localPosition The position stored locally on this device
     * @param remotePosition The position stored on the server
     */
    data class Conflict(
        val localPosition: PositionDomainModel,
        val remotePosition: PositionDomainModel,
    ) : ReadingProgressResult()
}

