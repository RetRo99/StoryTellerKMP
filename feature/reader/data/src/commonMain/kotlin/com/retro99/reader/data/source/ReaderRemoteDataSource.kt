package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.NetworkClient

@Factory
internal class ReaderRemoteDataSource(
    @Provided private val networkClient: NetworkClient,
    @Provided private val fileDownloader: EbookFileDownloader,
) : ReaderRemoteSource {

    override suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
    ): AppResult<String> {
        return fileDownloader.downloadEbook(ebookFilePath, bookUuid)
    }
}

