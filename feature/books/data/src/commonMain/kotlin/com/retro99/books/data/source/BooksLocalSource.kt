package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.BookLocalModel

interface BooksLocalSource {

    suspend fun getBooks(): AppResult<List<BookLocalModel>?>

    suspend fun getBook(uuid: String): AppResult<BookLocalModel?>

    suspend fun saveBooks(books: List<BookLocalModel>): CompletableResult

    suspend fun saveBook(book: BookLocalModel): CompletableResult

    suspend fun clearCache(): CompletableResult
}
