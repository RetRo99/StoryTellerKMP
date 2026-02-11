package com.retro99.reader.domain.model

/**
 * Represents the cache status for all media types of a book.
 */
data class MediaCacheStatusDomainModel(
    val isEbookCached: Boolean,
    val isAudiobookCached: Boolean,
    val isReadaloudCached: Boolean,
)

