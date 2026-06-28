package com.retro99.reader.ui.model

import com.retro99.reader.domain.model.BookmarkDomainModel

data class BookmarkUiModel(
    val id: String,
    val locatorHref: String,
    val locatorType: String?,
    val locatorTitle: String?,
    val progression: Double?,
    val totalProgression: Double?,
    val chapterIndex: Int?,
    val position: Int?,
    val createdAt: String,
)

fun BookmarkDomainModel.toUiModel(): BookmarkUiModel {
    return BookmarkUiModel(
        id = id,
        locatorHref = locatorHref,
        locatorType = locatorType,
        locatorTitle = locatorTitle,
        progression = progression,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        position = position,
        createdAt = createdAt,
    )
}
