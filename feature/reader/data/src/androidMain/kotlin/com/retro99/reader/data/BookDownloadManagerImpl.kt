package com.retro99.reader.data

import android.content.Context
import com.retro99.reader.data.download.DownloadForegroundService
import com.retro99.reader.data.download.DownloadStateHolder
import com.retro99.reader.data.source.EbookFileDownloader
import com.retro99.reader.domain.BookDownloadManager
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadKey
import com.retro99.reader.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Android implementation of [BookDownloadManager].
 *
 * Uses a ForegroundService to run downloads, ensuring they continue
 * even when the app is killed. State is held in [DownloadStateHolder]
 * which survives service restarts.
 */
@Single(binds = [BookDownloadManager::class])
actual class BookDownloadManagerImpl(
    @Provided private val context: Context,
    @Provided private val fileDownloader: EbookFileDownloader,
    @Provided private val downloadStateHolder: DownloadStateHolder,
) : BookDownloadManager {

    override fun observeDownloadState(
        bookUuid: String,
        bookType: BookType,
    ): Flow<DownloadState> {
        return downloadStateHolder.observeDownloadState(bookUuid, bookType)
            .map { state -> resolveState(state, bookUuid, bookType) }
            .distinctUntilChanged()
    }

    override fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadState>> {
        return downloadStateHolder.observeAllDownloads()
    }

    override suspend fun startDownload(
        bookUuid: String,
        bookType: BookType,
        filePath: String,
        bookTitle: String,
    ) {
        // Check if already cached
        if (fileDownloader.isEbookCached(bookUuid, bookType)) {
            downloadStateHolder.markCached(bookUuid, bookType)
            return
        }

        // Check if already downloading
        val currentState = downloadStateHolder.getDownloadState(bookUuid, bookType)
        if (currentState is DownloadState.Downloading) {
            return
        }

        // Start the foreground service
        val intent = DownloadForegroundService.createStartIntent(
            context = context,
            bookUuid = bookUuid,
            bookType = bookType,
            filePath = filePath,
            bookTitle = bookTitle,
        )
        context.startForegroundService(intent)
    }

    override suspend fun cancelDownload(bookUuid: String, bookType: BookType) {
        val intent = DownloadForegroundService.createCancelIntent(
            context = context,
            bookUuid = bookUuid,
            bookType = bookType,
        )
        context.startService(intent)
    }

    override fun clearError(bookUuid: String, bookType: BookType) {
        downloadStateHolder.clearError(bookUuid, bookType)
    }

    override fun getDownloadState(bookUuid: String, bookType: BookType): DownloadState {
        val state = downloadStateHolder.getDownloadState(bookUuid, bookType)
        return resolveState(state, bookUuid, bookType)
    }

    /**
     * Resolves the actual download state by checking if the file is cached
     * when the state is Idle.
     */
    private fun resolveState(
        state: DownloadState,
        bookUuid: String,
        bookType: BookType,
    ): DownloadState {
        return if (state is DownloadState.Idle &&
            fileDownloader.isEbookCached(bookUuid, bookType)
        ) {
            DownloadState.Cached
        } else {
            state
        }
    }

    override suspend fun deleteCache(bookUuid: String, bookType: BookType): Boolean {
        val deleted = fileDownloader.deleteEbookCache(bookUuid, bookType)
        if (deleted) {
            downloadStateHolder.markIdle(bookUuid, bookType)
        }
        return deleted
    }
}

