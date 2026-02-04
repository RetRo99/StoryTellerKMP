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
