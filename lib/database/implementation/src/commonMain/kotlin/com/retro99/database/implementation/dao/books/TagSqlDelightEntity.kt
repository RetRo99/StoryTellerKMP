package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.TagEntity

/**
 * SQLDelight entity for tags table.
 */
data class TagSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : TagEntity

