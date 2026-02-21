package com.retro99.books.domain.model

import kotlin.math.abs

/**
 * Represents progress and cache information for a book.
 */
data class BookProgressInfoDomainModel(
    val bookUuid: String,
    /**
     * Local reading progress as a value between 0.0 and 1.0.
     * Null if no local progress has been recorded.
     */
    val localProgression: Double?,
    /**
     * Remote reading progress as a value between 0.0 and 1.0.
     * Null if no remote progress exists or couldn't be fetched.
     */
    val remoteProgression: Double?,
    /**
     * Whether the ebook is cached locally.
     */
    val isEbookCached: Boolean,
    /**
     * Whether the audiobook is cached locally.
     */
    val isAudiobookCached: Boolean,
    /**
     * Whether the readaloud is cached locally.
     */
    val isReadaloudCached: Boolean,
) {
    /**
     * Returns true if any media type is cached.
     */
    val hasAnyCached: Boolean
        get() = isEbookCached || isAudiobookCached || isReadaloudCached

    /**
     * Returns true if there's a conflict between local and remote progress.
     * A conflict exists when both have progress and they differ by more than 1%.
     */
    val hasConflict: Boolean
        get() {
            val local = localProgression ?: return false
            val remote = remoteProgression ?: return false
            return abs(local - remote) > PROGRESSION_CONFLICT_THRESHOLD
        }

    /**
     * Returns the progress to display (prefers local if available).
     */
    val displayProgression: Double?
        get() = localProgression ?: remoteProgression

    companion object {
        /**
         * Minimum difference in progression (0.0 to 1.0) to consider as a conflict.
         * 1% difference threshold to avoid false positives from floating point issues.
         */
        private const val PROGRESSION_CONFLICT_THRESHOLD = 0.01
    }
}

