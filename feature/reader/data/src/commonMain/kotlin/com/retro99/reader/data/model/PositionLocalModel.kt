package com.retro99.reader.data.model

import com.retro99.database.api.books.PositionEntity
import com.retro99.reader.domain.model.ReadingProgressDomainModel

data class PositionLocalModel(
    override val bookUuid: String,
    override val uuid: String? = null,
    override val timestamp: Long? = null,
    override val createdAt: String? = null,
    override val updatedAt: String? = null,
    override val locatorHref: String?,
    override val locatorType: String?,
    override val locatorTitle: String?,
    override val locatorTarget: Int? = null,
    override val audioTimestampMs: Long? = null,
    override val chapterIndex: Int?,
    override val progression: Double?,
    override val totalChapters: Int?,
    override val totalDurationMs: Long? = null,
    override val totalProgression: Double?,
    override val position: Int? = null,
) : PositionEntity

fun PositionLocalModel.toDomain(): ReadingProgressDomainModel {
    return ReadingProgressDomainModel(
        bookUuid = bookUuid,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        progression = progression,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        totalChapters = totalChapters,
        lastReadAt = updatedAt ?: createdAt ?: "",
    )
}

fun ReadingProgressDomainModel.toLocal(): PositionLocalModel {
    return PositionLocalModel(
        bookUuid = bookUuid,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        progression = progression,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        totalChapters = totalChapters,
        updatedAt = lastReadAt,
    )
}

fun PositionEntity.toLocalModel(): PositionLocalModel {
    return PositionLocalModel(
        bookUuid = bookUuid,
        uuid = uuid,
        timestamp = timestamp,
        createdAt = createdAt,
        updatedAt = updatedAt,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        locatorTarget = locatorTarget,
        audioTimestampMs = audioTimestampMs,
        chapterIndex = chapterIndex,
        progression = progression,
        totalChapters = totalChapters,
        totalDurationMs = totalDurationMs,
        totalProgression = totalProgression,
        position = position,
    )
}

