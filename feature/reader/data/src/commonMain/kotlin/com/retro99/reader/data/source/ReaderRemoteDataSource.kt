package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.data.model.PositionApiModel
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.NetworkClient
import retro99.network.api.get
import retro99.network.api.post

@Single(binds = [ReaderRemoteSource::class])
internal class ReaderRemoteDataSource(
    @Provided private val networkClient: NetworkClient,
) : ReaderRemoteSource {

    override suspend fun getPosition(bookUuid: String): AppResult<PositionApiModel?> {
        return networkClient.get(path = "/api/v2/books/$bookUuid/positions")
    }

    override suspend fun updatePosition(
        bookUuid: String,
        position: PositionApiModel,
    ): CompletableResult {
        return networkClient.post(path = "/api/v2/books/$bookUuid/positions", body = position)
    }
}

