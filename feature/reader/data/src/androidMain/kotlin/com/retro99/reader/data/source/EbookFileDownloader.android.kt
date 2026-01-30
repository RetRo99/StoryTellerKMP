package com.retro99.reader.data.source

import android.content.Context
import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
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
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val localFile = File(ebooksDir, "$bookUuid.epub")

        networkClient.downloadFile(
            path = "/api/v2/books/$bookUuid/files",
            queryBuilder = { "format" to "ebook" },
        ).map { bytes ->
            localFile.writeBytes(bytes)
            localFile.absolutePath
        }
    }

    actual fun getCachedEbookPath(bookUuid: String): String? {
        val file = File(ebooksDir, "$bookUuid.epub")
        return if (file.exists()) file.absolutePath else null
    }

    actual fun isEbookCached(bookUuid: String): Boolean {
        return File(ebooksDir, "$bookUuid.epub").exists()
    }
}

