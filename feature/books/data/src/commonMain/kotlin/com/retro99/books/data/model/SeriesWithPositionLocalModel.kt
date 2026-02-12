package com.retro99.books.data.model

import com.retro99.books.domain.model.SeriesDomainModel
import com.retro99.database.api.books.SeriesWithPositionEntity

data class SeriesWithPositionLocalModel(
    override val uuid: String,
    override val name: String,
    override val featured: Int?,
    override val position: Double?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : SeriesWithPositionEntity

fun SeriesWithPositionLocalModel.toDomain(): SeriesDomainModel {
    return SeriesDomainModel(
        uuid = uuid,
        name = name,
        featured = featured,
        position = position,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SeriesDomainModel.toLocal(): SeriesWithPositionLocalModel {
    return SeriesWithPositionLocalModel(
        uuid = uuid,
        name = name,
        featured = featured,
        position = position,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

