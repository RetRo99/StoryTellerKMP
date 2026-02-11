package com.retro99.reader.data.source

import android.content.Context
import com.retro99.base.result.AppResult
import com.retro99.reader.domain.model.BookType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.NetworkClient
import java.io.File

@Single
actual class EbookFileDownloader(
    @Provided private val context: Context,
    @Provided private val networkClient: NetworkClient,
) {

    private val ebooksDir: File
        get() = File(context.filesDir, "ebooks").apply { mkdirs() }

    actual suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val localFile = File(ebooksDir, getFileName(bookUuid, bookType))

        // Use streaming download to avoid loading large files into memory
        networkClient.downloadFileToPath(
            path = "/api/v2/books/$bookUuid/files",
            destinationPath = localFile.absolutePath,
            queryBuilder = { "format" to bookType.value },
        )
    }

    actual suspend fun downloadEbookWithProgress(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
        onProgress: (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val localFile = File(ebooksDir, getFileName(bookUuid, bookType))

        // Use streaming download with progress reporting
        networkClient.downloadFileToPathWithProgress(
            path = "/api/v2/books/$bookUuid/files",
            destinationPath = localFile.absolutePath,
            onProgress = onProgress,
            queryBuilder = { "format" to bookType.value },
        )
    }

    actual fun getCachedEbookPath(bookUuid: String, bookType: BookType): String? {
        val file = File(ebooksDir, getFileName(bookUuid, bookType))
        return if (file.exists()) file.absolutePath else null
    }

    actual fun isEbookCached(bookUuid: String, bookType: BookType): Boolean {
        return File(ebooksDir, getFileName(bookUuid, bookType)).exists()
    }

    actual fun deleteEbookCache(bookUuid: String, bookType: BookType): Boolean {
        val file = File(ebooksDir, getFileName(bookUuid, bookType))
        return if (file.exists()) file.delete() else true
    }

    private fun getFileName(bookUuid: String, bookType: BookType): String {
        return "${bookUuid}_${bookType.value}.epub"
    }
}

