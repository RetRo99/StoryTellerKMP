package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.StatusEntity

/**
 * SQLDelight entity for statuses table.
 */
data class StatusSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : StatusEntity

