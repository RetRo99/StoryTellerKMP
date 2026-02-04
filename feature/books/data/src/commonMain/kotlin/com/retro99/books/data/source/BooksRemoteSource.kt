package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.BookApiModel
import com.retro99.reader.data.model.PositionApiModel

interface BooksRemoteSource {

    suspend fun getBooks(): AppResult<List<BookApiModel>>

    suspend fun getBook(uuid: String): AppResult<BookApiModel>

    suspend fun getPosition(uuid: String): AppResult<PositionApiModel?>

    suspend fun updatePosition(uuid: String, position: PositionApiModel): CompletableResult
}

