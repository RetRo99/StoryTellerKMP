package com.retro99.server.storyteller.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.BooksDatabase
import com.retro99.server.api.ServerBook
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Implementation of ServerBooksLocalSource that uses BooksDatabase for storage.
 * Caches ServerBook data per-server using the database's server-aware methods.
 */
@Single(binds = [ServerBooksLocalSource::class])
internal class ServerBooksLocalDataSource(
    @Provided private val booksDatabase: BooksDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : ServerBooksLocalSource {

    override suspend fun getBooks(serverId: String): AppResult<List<ServerBook>?> {
        return databaseExecutor.executeDatabaseOperation {
            val books = booksDatabase.getBooksByServer(serverId)
            if (books.isEmpty()) {
                null
            } else {
                // We need the baseUrl to build cover URLs, but we don't have it here
                // The caller should handle URL building or we store it
                books.map { it.toServerBook(baseUrl = null) }
            }
        }
    }

    override suspend fun getBook(serverId: String, uuid: String): AppResult<ServerBook?> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.getBookByServerAndUuid(serverId, uuid)?.toServerBook(baseUrl = null)
        }
    }

    override suspend fun saveBooks(serverId: String, books: List<ServerBook>): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            books.forEach { book ->
                booksDatabase.upsertBook(book.toEntity())
            }
        }
    }

    override suspend fun saveBook(serverId: String, book: ServerBook): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.upsertBook(book.toEntity())
        }
    }

    override suspend fun clearCache(serverId: String): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.deleteBooksByServer(serverId)
        }
    }
}

