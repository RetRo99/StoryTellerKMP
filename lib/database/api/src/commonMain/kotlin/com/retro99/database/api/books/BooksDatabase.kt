package com.retro99.database.api.books

import kotlinx.coroutines.flow.Flow

interface BooksDatabase {

    suspend fun upsertBooks(books: List<BookEntity>)

    suspend fun upsertBook(book: BookEntity)

    fun getAllBooks(): Flow<List<BookEntity>>

    suspend fun getBookByUuid(uuid: String): BookEntity?

    suspend fun deleteAllBooks()

    suspend fun deleteBook(uuid: String)

    suspend fun getBooksCount(): Int

    suspend fun getBooksOlderThan(timestamp: Long): List<BookEntity>
}

