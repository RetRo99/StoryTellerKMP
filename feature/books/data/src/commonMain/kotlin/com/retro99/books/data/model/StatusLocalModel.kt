package com.retro99.books.data.model

import com.retro99.books.domain.model.StatusDomainModel
import com.retro99.database.api.books.StatusEntity

data class StatusLocalModel(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : StatusEntity

fun StatusLocalModel.toDomain(): StatusDomainModel {
    return StatusDomainModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun StatusDomainModel.toLocal(): StatusLocalModel {
    return StatusLocalModel(
        uuid = uuid,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

