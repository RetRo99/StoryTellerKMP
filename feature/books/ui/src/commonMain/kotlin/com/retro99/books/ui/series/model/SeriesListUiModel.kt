package com.retro99.books.ui.series.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesListUiModel(
    val uuid: String,
    val name: String,
    val featured: Int?,
)

