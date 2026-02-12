package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.BookSeriesEntity

/**
 * SQLDelight entity for book-series relationship.
 */
data class BookSeriesSqlDelightEntity(
    override val bookUuid: String,
    override val seriesUuid: String,
    override val position: Double?,
) : BookSeriesEntity

