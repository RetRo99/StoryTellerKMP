package com.retro99.database.api.books

/**
 * Database interface for author-related operations.
 * Stores authors (creators) fetched from the /api/v2/creators endpoint.
 */
interface AuthorsDatabase {

    suspend fun upsertAuthor(author: PersonEntity)

    suspend fun getAllAuthors(): List<PersonEntity>

    suspend fun getAuthorByUuid(uuid: String): PersonEntity?

    suspend fun deleteAllAuthors()

    suspend fun deleteAuthor(uuid: String)

    suspend fun getAuthorsCount(): Int
}

