package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.data.model.BookApiModel

interface BooksRemoteSource {

    suspend fun getBooks(): AppResult<List<BookApiModel>>

    suspend fun getBook(uuid: String): AppResult<BookApiModel>
}

