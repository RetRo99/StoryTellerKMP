package com.retro99.reader.domain.model

/**
 * Represents an opened ebook ready for reading.
 * This is a platform-agnostic representation of the ebook.
 */
data class EbookDomainModel(
    val bookUuid: String,
    val title: String,
    val filePath: String,
    val coverUrl: String?,
    val totalProgression: Float,
)

