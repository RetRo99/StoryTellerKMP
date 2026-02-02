package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.PersonEntity

/**
 * SQLDelight entity for persons table.
 */
data class PersonSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val fileAs: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : PersonEntity

