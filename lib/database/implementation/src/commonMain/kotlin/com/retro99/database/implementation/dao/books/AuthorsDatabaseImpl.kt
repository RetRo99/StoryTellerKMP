package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.AuthorsDatabase
import com.retro99.database.api.books.PersonEntity

/**
 * Implementation of AuthorsDatabase using SQLDelight.
 */
internal class AuthorsDatabaseImpl(
    private val dao: AuthorsSqlDelightDao,
) : AuthorsDatabase {

    override suspend fun upsertAuthor(author: PersonEntity) {
        dao.upsertAuthor(author.toSqlDelightEntity())
    }

    override suspend fun getAllAuthors(): List<PersonEntity> {
        return dao.getAllAuthors()
    }

    override suspend fun getAuthorByUuid(uuid: String): PersonEntity? {
        return dao.getAuthorByUuid(uuid)
    }

    override suspend fun deleteAllAuthors() {
        dao.deleteAllAuthors()
    }

    override suspend fun deleteAuthor(uuid: String) {
        dao.deleteAuthor(uuid)
    }

    override suspend fun getAuthorsCount(): Int {
        return dao.getAuthorsCount().toInt()
    }

    override suspend fun clearAllData() {
        dao.deleteAllAuthors()
    }

    private fun PersonEntity.toSqlDelightEntity(): PersonSqlDelightEntity {
        return PersonSqlDelightEntity(
            uuid = uuid,
            name = name,
            fileAs = fileAs,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

