package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.CollectionEntity

/**
 * SQLDelight entity for collections table.
 */
data class CollectionSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : CollectionEntity

