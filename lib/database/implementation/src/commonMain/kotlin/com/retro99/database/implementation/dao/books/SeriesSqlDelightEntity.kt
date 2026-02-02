package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.SeriesEntity

/**
 * SQLDelight entity for series table.
 */
data class SeriesSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val featured: Int?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : SeriesEntity

