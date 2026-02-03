package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.BookApiModel
import com.retro99.books.data.model.PositionApiModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.NetworkClient
import retro99.network.api.get
import retro99.network.api.post

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

    override suspend fun getPosition(uuid: String): AppResult<PositionApiModel?> {
        return networkClient.get(path = "/api/v2/books/$uuid/positions")
    }

    override suspend fun updatePosition(
        uuid: String,
        position: PositionApiModel,
    ): CompletableResult {
        return networkClient.post(path = "/api/v2/books/$uuid/positions", body = position)
    }
}

