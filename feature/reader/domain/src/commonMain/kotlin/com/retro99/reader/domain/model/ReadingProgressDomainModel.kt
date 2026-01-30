package com.retro99.reader.domain.model

/**
 * Represents the reading progress for an ebook.
 */
data class ReadingProgressDomainModel(
    val bookUuid: String,
    val locator: String,
    val progression: Float,
    val lastReadAt: String,
)

