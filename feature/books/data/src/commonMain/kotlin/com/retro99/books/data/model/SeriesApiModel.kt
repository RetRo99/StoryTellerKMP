package com.retro99.books.data.model

import com.retro99.books.domain.model.SeriesDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("name")
    val name: String,

    @SerialName("featured")
    val featured: Int? = null,

    @SerialName("position")
    val position: Double? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

fun SeriesApiModel.toDomain(): SeriesDomainModel {
    return SeriesDomainModel(
        uuid = uuid,
        name = name,
        featured = featured,
        position = position,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

