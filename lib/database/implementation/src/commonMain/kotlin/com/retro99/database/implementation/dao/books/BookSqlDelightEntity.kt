package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.BookEntity

/**
 * SQLDelight entity for books table.
 *
 * Only queryable fields are stored as columns.
 * Full book data is stored as JSON for easy retrieval.
 */
data class BookSqlDelightEntity(
    override val uuid: String,
    override val title: String,
    override val id: Long,
    override val rating: Float?,
    override val dataJson: String,
) : BookEntity

