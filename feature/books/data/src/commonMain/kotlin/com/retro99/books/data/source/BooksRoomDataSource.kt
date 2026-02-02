package com.retro99.books.data.source

import com.retro99.base.nowMillis
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.BooksDatabase
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

private const val CACHE_VALIDITY_DURATION_MS = 30 * 60 * 1000L // 30 minutes

@Single(binds = [BooksLocalSource::class])
internal class BooksRoomDataSource(
    @Provided private val booksDatabase: BooksDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : BooksLocalSource {

    override suspend fun getBooks(): AppResult<List<BookEntity>?> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.getAllBooks().takeIf { it.isNotEmpty() }
        }
    }

    override suspend fun getBook(uuid: String): AppResult<BookEntity?> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.getBookByUuid(uuid)
        }
    }

    override suspend fun saveBooks(books: List<BookEntity>): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.upsertBooks(books)
        }
    }

    override suspend fun saveBook(book: BookEntity): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.upsertBook(book)
        }
    }

    override suspend fun clearCache(): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.deleteAllBooks()
        }
    }

    override suspend fun isCacheValid(): AppResult<Boolean> {
        return databaseExecutor.executeDatabaseOperation {
            val count = booksDatabase.getBooksCount()
            if (count == 0) return@executeDatabaseOperation false

            val oldBooks = booksDatabase.getBooksOlderThan(nowMillis() - CACHE_VALIDITY_DURATION_MS)
            oldBooks.isEmpty()
        }
    }
}

