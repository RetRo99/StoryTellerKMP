package com.retro99.books.data.model

import com.retro99.books.domain.model.MediaFileDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaFileApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("filepath")
    val filepath: String,

    @SerialName("missing")
    val missing: Int? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

fun MediaFileApiModel.toDomain(): MediaFileDomainModel {
    return MediaFileDomainModel(
        uuid = uuid,
        filepath = filepath,
        missing = missing,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

