package com.retro99.reader.data.model

import com.retro99.database.api.books.ReadingProgressEntity
import com.retro99.reader.domain.model.ReadingProgressDomainModel

data class ReadingProgressLocalModel(
    override val bookUuid: String,
    override val locatorHref: String?,
    override val locatorType: String?,
    override val locatorTitle: String?,
    override val progression: Double?,
    override val totalProgression: Double?,
    override val chapterIndex: Int?,
    override val totalChapters: Int?,
    override val lastReadAt: String,
    override val audioTimestampMs: Long? = null,
) : ReadingProgressEntity

fun ReadingProgressLocalModel.toDomain(): ReadingProgressDomainModel {
    return ReadingProgressDomainModel(
        bookUuid = bookUuid,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        progression = progression,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        totalChapters = totalChapters,
        lastReadAt = lastReadAt,
    )
}

fun ReadingProgressDomainModel.toLocal(): ReadingProgressLocalModel {
    return ReadingProgressLocalModel(
        bookUuid = bookUuid,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        progression = progression,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        totalChapters = totalChapters,
        lastReadAt = lastReadAt,
    )
}

fun ReadingProgressEntity.toLocalModel(): ReadingProgressLocalModel {
    return ReadingProgressLocalModel(
        bookUuid = bookUuid,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        progression = progression,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        totalChapters = totalChapters,
        lastReadAt = lastReadAt,
    )
}

