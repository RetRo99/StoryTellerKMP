package com.retro99.books.data.model

import com.retro99.books.domain.model.CollectionDomainModel
import com.retro99.database.api.books.CollectionEntity

data class CollectionLocalModel(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : CollectionEntity

fun CollectionLocalModel.toDomain(): CollectionDomainModel {
    return CollectionDomainModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun CollectionDomainModel.toLocal(): CollectionLocalModel {
    return CollectionLocalModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

