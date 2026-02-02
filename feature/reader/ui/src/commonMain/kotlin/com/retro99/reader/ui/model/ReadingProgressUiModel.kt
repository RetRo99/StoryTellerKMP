package com.retro99.reader.ui.model

import com.retro99.reader.domain.model.ReadingProgressDomainModel

data class ReadingProgressUiModel(
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

fun ReadingProgressDomainModel.toUiModel(): ReadingProgressUiModel = ReadingProgressUiModel(
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

fun ReadingProgressUiModel.toDomainModel(): ReadingProgressDomainModel = ReadingProgressDomainModel(
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

