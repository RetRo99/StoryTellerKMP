package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.data.model.BookApiModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.NetworkClient

@Factory
internal class BooksRemoteDataSource(
    @Provided private val networkClient: NetworkClient,
) : BooksRemoteSource {

    override suspend fun getBooks(): AppResult<List<BookApiModel>> {
        return networkClient.get(path = "/api/v2/books")
    }

    override suspend fun getBook(uuid: String): AppResult<BookApiModel> {
        return networkClient.get(path = "/api/v2/books/$uuid")
    }
}

