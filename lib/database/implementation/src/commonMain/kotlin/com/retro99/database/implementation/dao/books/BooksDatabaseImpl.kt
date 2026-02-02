package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.BooksDatabase

internal class BooksDatabaseImpl(
    private val sqlDelightDao: BooksSqlDelightDao,
) : BooksDatabase {

    override suspend fun upsertBooks(books: List<BookEntity>) {
        sqlDelightDao.upsertBooks(books.map { it.toSqlDelightEntity() })
    }

    override suspend fun upsertBook(book: BookEntity) {
        sqlDelightDao.upsertBook(book.toSqlDelightEntity())
    }

    override suspend fun getAllBooks(): List<BookEntity> {
        return sqlDelightDao.getAllBooks()
    }

    override suspend fun getBookByUuid(uuid: String): BookEntity? {
        return sqlDelightDao.getBookByUuid(uuid)
    }

    override suspend fun deleteAllBooks() {
        sqlDelightDao.deleteAllBooks()
    }

    override suspend fun deleteBook(uuid: String) {
        sqlDelightDao.deleteBook(uuid)
    }

    override suspend fun getBooksCount(): Int {
        return sqlDelightDao.getBooksCount().toInt()
    }

    override suspend fun getBooksOlderThan(timestamp: Long): List<BookEntity> {
        return sqlDelightDao.getBooksOlderThan(timestamp)
    }

    private fun BookEntity.toSqlDelightEntity(): BookSqlDelightEntity {
        return BookSqlDelightEntity(
            uuid = uuid,
            title = title,
            id = id,
            rating = rating,
            dataJson = dataJson,
            cachedAt = cachedAt,
        )
    }
}

