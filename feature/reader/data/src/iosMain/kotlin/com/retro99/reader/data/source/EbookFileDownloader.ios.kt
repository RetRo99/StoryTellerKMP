package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import com.retro99.reader.domain.model.BookType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import retro99.network.api.NetworkClient

@Single
actual class EbookFileDownloader(
    @Provided private val networkClient: NetworkClient,
) {

    private val ebooksDir: String
        @OptIn(ExperimentalForeignApi::class)
        get() {
            val paths = NSSearchPathForDirectoriesInDomains(
                NSCachesDirectory,
                NSUserDomainMask,
                true,
            )
            val cachesDir = paths.firstOrNull() as? String ?: ""
            val ebooksPath = "$cachesDir/ebooks"
            NSFileManager.defaultManager.createDirectoryAtPath(
                ebooksPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
            return ebooksPath
        }

    actual suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val localPath = "$ebooksDir/${getFileName(bookUuid, bookType)}"

        // Use streaming download to avoid loading large files into memory
        networkClient.downloadFileToPath(
            path = "/api/v2/books/$bookUuid/files",
            destinationPath = localPath,
            queryBuilder = { "format" to bookType.value },
        )
    }

    actual suspend fun downloadEbookWithProgress(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
        onProgress: (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val localPath = "$ebooksDir/${getFileName(bookUuid, bookType)}"

        // Use streaming download with progress reporting
        networkClient.downloadFileToPathWithProgress(
            path = "/api/v2/books/$bookUuid/files",
            destinationPath = localPath,
            onProgress = onProgress,
            queryBuilder = { "format" to bookType.value },
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun getCachedEbookPath(bookUuid: String, bookType: BookType): String? {
        val path = "$ebooksDir/${getFileName(bookUuid, bookType)}"
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) path else null
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun isEbookCached(bookUuid: String, bookType: BookType): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(
            "$ebooksDir/${getFileName(bookUuid, bookType)}",
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteEbookCache(bookUuid: String, bookType: BookType): Boolean {
        val path = "$ebooksDir/${getFileName(bookUuid, bookType)}"
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) {
            NSFileManager.defaultManager.removeItemAtPath(path, error = null)
        } else {
            true
        }
    }

    private fun getFileName(bookUuid: String, bookType: BookType): String {
        return "${bookUuid}_${bookType.value}.epub"
    }
}

