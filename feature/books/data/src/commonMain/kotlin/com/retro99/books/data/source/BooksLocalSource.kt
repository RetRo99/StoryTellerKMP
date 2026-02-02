package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.books.BookEntity
import kotlinx.coroutines.flow.Flow

interface BooksLocalSource {

    fun getBooks(): Flow<List<BookEntity>>

    suspend fun getBook(uuid: String): AppResult<BookEntity?>

    suspend fun saveBooks(books: List<BookEntity>): AppResult<Unit>

    suspend fun saveBook(book: BookEntity): CompletableResult

    suspend fun clearCache(): CompletableResult

    suspend fun isCacheValid(): AppResult<Boolean>
}

