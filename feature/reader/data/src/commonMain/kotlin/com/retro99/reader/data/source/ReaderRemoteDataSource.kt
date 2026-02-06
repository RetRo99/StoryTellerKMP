package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.data.model.PositionApiModel
import com.retro99.reader.domain.model.BookType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.NetworkClient
import retro99.network.api.get
import retro99.network.api.post

@Single(binds = [ReaderRemoteSource::class])
internal class ReaderRemoteDataSource(
    @Provided private val networkClient: NetworkClient,
    @Provided private val fileDownloader: EbookFileDownloader,
) : ReaderRemoteSource {

    override suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
    ): AppResult<String> {
        return fileDownloader.downloadEbook(ebookFilePath, bookUuid, bookType)
    }

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

