package com.retro99.reader.ui.model

import com.retro99.reader.domain.model.ReadingProgressDomainModel

data class ReadingProgressUiModel(
    val bookUuid: String,
    val locator: String,
    val progression: Float,
    val lastReadAt: String,
)

fun ReadingProgressDomainModel.toUiModel(): ReadingProgressUiModel = ReadingProgressUiModel(
    bookUuid = bookUuid,
    locator = locator,
    progression = progression,
    lastReadAt = lastReadAt,
)

fun ReadingProgressUiModel.toDomainModel(): ReadingProgressDomainModel = ReadingProgressDomainModel(
    bookUuid = bookUuid,
    locator = locator,
    progression = progression,
    lastReadAt = lastReadAt,
)

