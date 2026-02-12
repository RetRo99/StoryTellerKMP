package com.retro99.books.data.model

import com.retro99.books.domain.model.PersonDomainModel
import com.retro99.database.api.books.PersonEntity

data class PersonLocalModel(
    override val uuid: String,
    override val name: String,
    override val fileAs: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : PersonEntity

fun PersonLocalModel.toDomain(): PersonDomainModel {
    return PersonDomainModel(
        uuid = uuid,
        id = null,
        name = name,
        fileAs = fileAs,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun PersonDomainModel.toLocal(): PersonLocalModel {
    return PersonLocalModel(
        uuid = uuid,
        name = name,
        fileAs = fileAs,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun PersonEntity.toLocalModel(): PersonLocalModel {
    return PersonLocalModel(
        uuid = uuid,
        name = name,
        fileAs = fileAs,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

