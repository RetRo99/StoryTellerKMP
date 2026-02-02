package com.retro99.database.api.books

interface BooksDatabase {

    suspend fun upsertBooks(books: List<BookEntity>)

    suspend fun upsertBook(book: BookEntity)

    suspend fun getAllBooks(): List<BookEntity>

    suspend fun getBookByUuid(uuid: String): BookEntity?

    suspend fun deleteAllBooks()

    suspend fun deleteBook(uuid: String)

    suspend fun getBooksCount(): Int
}

