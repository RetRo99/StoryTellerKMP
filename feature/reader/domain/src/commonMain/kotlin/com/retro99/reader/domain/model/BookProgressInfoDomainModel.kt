package com.retro99.reader.domain.model

/**
 * Represents progress and cache information for a book.
 */
data class BookProgressInfoDomainModel(
    val bookUuid: String,
    /**
     * Total reading progress as a value between 0.0 and 1.0.
     * Null if no progress has been recorded.
     */
    val totalProgression: Double?,
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
}

