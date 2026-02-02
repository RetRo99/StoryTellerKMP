package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.books.BookEntity

interface BooksLocalSource {

    suspend fun getBooks(): AppResult<List<BookEntity>?>

    suspend fun getBook(uuid: String): AppResult<BookEntity?>

    suspend fun saveBooks(books: List<BookEntity>): CompletableResult

    suspend fun saveBook(book: BookEntity): CompletableResult

    suspend fun clearCache(): CompletableResult

    suspend fun isCacheValid(): AppResult<Boolean>
}

