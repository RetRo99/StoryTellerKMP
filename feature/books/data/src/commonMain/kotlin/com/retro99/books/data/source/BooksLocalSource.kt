package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.data.model.BookApiModel
import kotlinx.coroutines.flow.Flow

interface BooksLocalSource {

    fun getBooks(): Flow<List<BookApiModel>>

    suspend fun getBook(uuid: String): AppResult<BookApiModel>

    suspend fun saveBooks(books: List<BookApiModel>): AppResult<Unit>

    suspend fun saveBook(book: BookApiModel): AppResult<Unit>

    suspend fun clearCache(): AppResult<Unit>

    suspend fun isCacheValid(): AppResult<Boolean>
}

