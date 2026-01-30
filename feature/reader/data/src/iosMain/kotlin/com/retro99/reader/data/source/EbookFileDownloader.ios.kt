package com.retro99.reader.data.source

import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
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

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val localPath = "$ebooksDir/$bookUuid.epub"

        networkClient.downloadFile(
            path = "/api/v2/books/$bookUuid/files",
            queryBuilder = { "format" to "ebook" },
        ).map { bytes ->
            bytes.usePinned { pinned ->
                val data = NSData.create(
                    bytes = pinned.addressOf(0),
                    length = bytes.size.toULong(),
                )
                data.writeToFile(localPath, atomically = true)
            }
            localPath
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun getCachedEbookPath(bookUuid: String): String? {
        val path = "$ebooksDir/$bookUuid.epub"
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) path else null
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun isEbookCached(bookUuid: String): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath("$ebooksDir/$bookUuid.epub")
    }
}

