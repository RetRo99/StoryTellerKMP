package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.PersonLocalModel
import com.retro99.books.data.model.toLocalModel
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.AuthorsDatabase
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [AuthorsLocalSource::class])
internal class AuthorsRoomDataSource(
    @Provided private val authorsDatabase: AuthorsDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : AuthorsLocalSource {

    override suspend fun getAuthors(): AppResult<List<PersonLocalModel>?> {
        return databaseExecutor.executeDatabaseOperation {
            val authors = authorsDatabase.getAllAuthors()
            if (authors.isEmpty()) {
                null
            } else {
                authors.map { it.toLocalModel() }
            }
        }
    }

    override suspend fun saveAuthors(authors: List<PersonLocalModel>): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            authors.forEach { author ->
                authorsDatabase.upsertAuthor(author)
            }
        }
    }

    override suspend fun clearCache(): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            authorsDatabase.deleteAllAuthors()
        }
    }
}

