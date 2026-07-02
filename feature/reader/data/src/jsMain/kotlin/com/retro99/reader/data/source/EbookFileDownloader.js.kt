package com.retro99.reader.data.source

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookType
import org.koin.core.annotation.Single

@Single
actual class EbookFileDownloader {

    actual suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
        serverId: String,
    ): AppResult<String> {
        return Err(AppError.NotFoundError("Downloads not yet supported on web"))
    }

    actual suspend fun downloadEbookWithProgress(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
        serverId: String,
        onProgress: suspend (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
    ): AppResult<String> {
        return Err(AppError.NotFoundError("Downloads not yet supported on web"))
    }

    actual fun getCachedEbookPath(bookUuid: String, bookType: BookType): String? = null

    actual fun isEbookCached(bookUuid: String, bookType: BookType): Boolean = false

    actual fun deleteEbookCache(bookUuid: String, bookType: BookType): Boolean = false
}
