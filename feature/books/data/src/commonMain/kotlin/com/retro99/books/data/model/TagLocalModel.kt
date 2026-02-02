package com.retro99.books.data.model

import com.retro99.books.domain.model.TagDomainModel
import com.retro99.database.api.books.TagEntity

data class TagLocalModel(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : TagEntity

fun TagLocalModel.toDomain(): TagDomainModel {
    return TagDomainModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun TagDomainModel.toLocal(): TagLocalModel {
    return TagLocalModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

