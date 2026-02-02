package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.BooksDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class BooksDatabaseImpl(
    private val roomDao: BooksRoomDao,
) : BooksDatabase {

    override suspend fun upsertBooks(books: List<BookEntity>) {
        roomDao.upsertBooks(books.map { it.toRoomEntity() })
    }

    override suspend fun upsertBook(book: BookEntity) {
        roomDao.upsertBook(book.toRoomEntity())
    }

    override fun getAllBooks(): Flow<List<BookEntity>> {
        return roomDao.getAllBooks().map { entities ->
            entities.map { it as BookEntity }
        }
    }

    override suspend fun getBookByUuid(uuid: String): BookEntity? {
        return roomDao.getBookByUuid(uuid)
    }

    override suspend fun deleteAllBooks() {
        roomDao.deleteAllBooks()
    }

    override suspend fun deleteBook(uuid: String) {
        roomDao.deleteBook(uuid)
    }

    override suspend fun getBooksCount(): Int {
        return roomDao.getBooksCount()
    }

    override suspend fun getBooksOlderThan(timestamp: Long): List<BookEntity> {
        return roomDao.getBooksOlderThan(timestamp)
    }

    private fun BookEntity.toRoomEntity(): BookRoomEntity {
        return BookRoomEntity(
            uuid = uuid,
            title = title,
            id = id,
            rating = rating,
            dataJson = dataJson,
            cachedAt = cachedAt,
        )
    }
}

