package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.BookApiModel

interface BooksLocalSource {

    suspend fun getBooks(): AppResult<List<BookApiModel>?>

    suspend fun getBook(uuid: String): AppResult<BookApiModel?>

    suspend fun saveBooks(books: List<BookApiModel>): CompletableResult

    suspend fun saveBook(book: BookApiModel): CompletableResult

    suspend fun clearCache(): CompletableResult
}
