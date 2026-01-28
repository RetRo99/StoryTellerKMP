package com.retro99.books.data.model

import com.retro99.books.domain.model.CollectionDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("name")
    val name: String,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

fun CollectionApiModel.toDomain(): CollectionDomainModel {
    return CollectionDomainModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

