package com.retro99.reader.domain.model

/**
 * Represents the reading progress for an ebook.
 */
data class ReadingProgressDomainModel(
    val bookUuid: String,
    val locatorHref: String?,
    val locatorType: String?,
    val locatorTitle: String?,
    val progression: Double?,
    val totalProgression: Double?,
    val chapterIndex: Int?,
    val totalChapters: Int?,
    val lastReadAt: String,
)

