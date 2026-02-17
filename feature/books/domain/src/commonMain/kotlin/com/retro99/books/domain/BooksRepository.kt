package com.retro99.books.domain

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.domain.model.BookDomainModel
import kotlinx.coroutines.flow.Flow

interface BooksRepository {

    fun getBooks(): Flow<AppResult<List<BookDomainModel>>>

    fun getBook(uuid: String): Flow<AppResult<BookDomainModel>>

    /**
     * Saves a book to the repository.
     * For imported books, this saves to local storage.
     * For Storyteller books, this will upload to the server (not yet implemented).
     */
    suspend fun saveBook(book: BookDomainModel): CompletableResult
}

