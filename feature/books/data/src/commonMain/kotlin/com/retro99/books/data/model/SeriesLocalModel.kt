package com.retro99.books.data.model

import com.retro99.books.domain.model.SeriesDomainModel
import com.retro99.database.api.books.SeriesEntity

data class SeriesLocalModel(
    override val uuid: String,
    override val name: String,
    override val featured: Int?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : SeriesEntity

fun SeriesLocalModel.toDomain(): SeriesDomainModel {
    return SeriesDomainModel(
        uuid = uuid,
        name = name,
        featured = featured,
        position = null,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SeriesDomainModel.toSeriesLocal(): SeriesLocalModel {
    return SeriesLocalModel(
        uuid = uuid,
        name = name,
        featured = featured,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun SeriesEntity.toLocalModel(): SeriesLocalModel {
    return SeriesLocalModel(
        uuid = uuid,
        name = name,
        featured = featured,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

