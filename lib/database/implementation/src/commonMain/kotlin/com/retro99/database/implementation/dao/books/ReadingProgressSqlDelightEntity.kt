package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.ReadingProgressEntity

/**
 * SQLDelight entity for reading_progress table.
 */
data class ReadingProgressSqlDelightEntity(
    override val bookUuid: String,
    override val locatorHref: String?,
    override val locatorType: String?,
    override val locatorTitle: String?,
    override val progression: Double?,
    override val totalProgression: Double?,
    override val chapterIndex: Int?,
    override val totalChapters: Int?,
    override val audioTimestampMs: Long?,
    override val lastReadAt: String,
) : ReadingProgressEntity

