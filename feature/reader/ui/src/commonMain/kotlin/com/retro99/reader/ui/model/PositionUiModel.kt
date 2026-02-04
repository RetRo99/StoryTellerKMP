package com.retro99.reader.ui.model

import com.retro99.reader.domain.model.PositionDomainModel
import kotlinx.serialization.Serializable

@Serializable
data class PositionUiModel(
    val createdAt: String?,
    val href: String,
    val type: String,
    val title: String?,
    val progression: Double?,
    val position: Int?,
    val totalProgression: Double?,
    val chapterIndex: Int?,
    val totalChapters: Int?,
)

fun PositionDomainModel.toUiModel(): PositionUiModel {
    return PositionUiModel(
        createdAt = createdAt,
        href = locatorHref ?: "",
        type = locatorType ?: "",
        title = locatorTitle,
        progression = progression,
        position = position,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        totalChapters = totalChapters,
    )
}