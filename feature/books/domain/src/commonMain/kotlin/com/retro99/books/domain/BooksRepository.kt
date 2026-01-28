package com.retro99.books.domain

import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookDomainModel

interface BooksRepository {

    suspend fun getBooks(): AppResult<List<BookDomainModel>>

    suspend fun getBook(uuid: String): AppResult<BookDomainModel>
}

