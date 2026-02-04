package com.retro99.books.ui.model

import kotlinx.serialization.Serializable

@Serializable
data class BookUiModel(
    val uuid: String,
    val title: String,
    val subtitle: String?,
    val coverUrl: String?,
    val authors: List<String>,
    val series: List<SeriesUiModel>,
    val tags: List<String>,
    val statusName: String?,
    val rating: Float?,
    val description: String?,
    val hasEbook: Boolean,
    val hasAudiobook: Boolean,
    val hasReadaloud: Boolean,
    val ebookFilepath: String?,
)

@Serializable
data class SeriesUiModel(
    val uuid: String,
    val name: String,
    val position: Int?,
)

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

