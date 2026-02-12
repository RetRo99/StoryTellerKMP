package com.retro99.database.implementation.dao.books

import com.retro99.database.implementation.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * SQLDelight DAO for authors table operations.
 * Handles standalone creators from /api/v2/creators endpoint.
 */
internal class AuthorsSqlDelightDao(
    private val database: AppDatabase,
) {
    private val authorQueries = database.authorQueries

    suspend fun upsertAuthor(author: PersonSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            authorQueries.upsertAuthor(
                uuid = author.uuid,
                name = author.name,
                file_as = author.fileAs,
                created_at = author.createdAt,
                updated_at = author.updatedAt,
            )
        }
    }

    suspend fun getAllAuthors(): List<PersonSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            authorQueries.getAllAuthors().executeAsList().map { row ->
                PersonSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    fileAs = row.file_as,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun getAuthorByUuid(uuid: String): PersonSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            authorQueries.getAuthorByUuid(uuid).executeAsOneOrNull()?.let { row ->
                PersonSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    fileAs = row.file_as,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun deleteAllAuthors() {
        withContext(Dispatchers.IO) {
            authorQueries.deleteAllAuthors()
        }
    }

    suspend fun deleteAuthor(uuid: String) {
        withContext(Dispatchers.IO) {
            authorQueries.deleteAuthor(uuid)
        }
    }

    suspend fun getAuthorsCount(): Long {
        return withContext(Dispatchers.IO) {
            authorQueries.getAuthorsCount().executeAsOne()
        }
    }
}

