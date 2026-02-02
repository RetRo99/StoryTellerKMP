package com.retro99.database.implementation.dao.books

import com.retro99.database.implementation.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * SQLDelight DAO for books table operations.
 */
internal class BooksSqlDelightDao(
    private val database: AppDatabase,
) {
    private val queries = database.bookQueries

    suspend fun upsertBooks(books: List<BookSqlDelightEntity>) {
        withContext(Dispatchers.IO) {
            database.transaction {
                books.forEach { book ->
                    queries.upsertBook(
                        uuid = book.uuid,
                        title = book.title,
                        id = book.id,
                        rating = book.rating?.toDouble(),
                        data_json = book.dataJson,
                        cached_at = book.cachedAt,
                    )
                }
            }
        }
    }

    suspend fun upsertBook(book: BookSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            queries.upsertBook(
                uuid = book.uuid,
                title = book.title,
                id = book.id,
                rating = book.rating?.toDouble(),
                data_json = book.dataJson,
                cached_at = book.cachedAt,
            )
        }
    }

    suspend fun getAllBooks(): List<BookSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            queries.getAllBooks { uuid, title, id, rating, data_json, cached_at ->
                BookSqlDelightEntity(
                    uuid = uuid,
                    title = title,
                    id = id,
                    rating = rating?.toFloat(),
                    dataJson = data_json,
                    cachedAt = cached_at,
                )
            }.executeAsList()
        }
    }

    suspend fun getBookByUuid(uuid: String): BookSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            queries.getBookByUuid(uuid) { uuid, title, id, rating, data_json, cached_at ->
                BookSqlDelightEntity(
                    uuid = uuid,
                    title = title,
                    id = id,
                    rating = rating?.toFloat(),
                    dataJson = data_json,
                    cachedAt = cached_at,
                )
            }.executeAsOneOrNull()
        }
    }

    suspend fun deleteAllBooks() {
        withContext(Dispatchers.IO) {
            queries.deleteAllBooks()
        }
    }

    suspend fun deleteBook(uuid: String) {
        withContext(Dispatchers.IO) {
            queries.deleteBook(uuid)
        }
    }

    suspend fun getBooksCount(): Long {
        return withContext(Dispatchers.IO) {
            queries.getBooksCount().executeAsOne()
        }
    }

    suspend fun getBooksOlderThan(timestamp: Long): List<BookSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            queries.getBooksOlderThan(timestamp) { uuid, title, id, rating, data_json, cached_at ->
                BookSqlDelightEntity(
                    uuid = uuid,
                    title = title,
                    id = id,
                    rating = rating?.toFloat(),
                    dataJson = data_json,
                    cachedAt = cached_at,
                )
            }.executeAsList()
        }
    }
}

