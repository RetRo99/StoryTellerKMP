package com.retro99.books.data.model

import com.retro99.books.domain.model.PersonDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("fileAs")
    val fileAs: String? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

fun PersonApiModel.toDomain(): PersonDomainModel {
    return PersonDomainModel(
        uuid = uuid,
        id = id,
        name = name,
        fileAs = fileAs,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

