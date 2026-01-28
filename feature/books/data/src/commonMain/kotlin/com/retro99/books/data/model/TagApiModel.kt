package com.retro99.books.data.model

import com.retro99.books.domain.model.TagDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("name")
    val name: String,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

fun TagApiModel.toDomain(): TagDomainModel {
    return TagDomainModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
