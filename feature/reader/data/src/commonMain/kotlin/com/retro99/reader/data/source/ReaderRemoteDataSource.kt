package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [ReaderRemoteSource::class])
internal class ReaderRemoteDataSource(
    @Provided private val fileDownloader: EbookFileDownloader,
) : ReaderRemoteSource {

    override suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
    ): AppResult<String> {
        return fileDownloader.downloadEbook(ebookFilePath, bookUuid)
    }
}

