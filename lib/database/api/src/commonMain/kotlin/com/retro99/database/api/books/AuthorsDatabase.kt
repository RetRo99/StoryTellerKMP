package com.retro99.database.api.books

import com.retro99.database.api.DataClearable

/**
 * Database interface for author-related operations.
 * Stores authors (creators) fetched from the /api/v2/creators endpoint.
 */
interface AuthorsDatabase : DataClearable {

    suspend fun upsertAuthor(author: PersonEntity)

    suspend fun getAllAuthors(): List<PersonEntity>

    suspend fun getAuthorByUuid(uuid: String): PersonEntity?

    suspend fun deleteAllAuthors()

    suspend fun deleteAuthor(uuid: String)

    suspend fun getAuthorsCount(): Int

    override suspend fun clearAllData()
}

