package com.retro99.reader.data

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.reader.data.source.EbookFileDownloader
import com.retro99.reader.domain.BookDownloadManager
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadKey
import com.retro99.reader.domain.model.DownloadStateDomainModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * iOS implementation of [BookDownloadManager].
 *
 * Uses URLSession-based downloads through the NetworkClient.
 * iOS has better background handling for network requests, so downloads
 * can continue when the app is backgrounded.
 *
 * TODO: For full background download support that survives app termination,
 * enhance to use URLSession with background configuration.
 */
@Single(binds = [BookDownloadManager::class])
actual class BookDownloadManagerImpl(
    @Provided private val fileDownloader: EbookFileDownloader,
) : BookDownloadManager {

    /**
     * Application-scoped coroutine scope for downloads.
     * Uses SupervisorJob so one failed download doesn't cancel others.
     */
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Holds the current state of all downloads.
     */
    private val downloadStates = MutableStateFlow<Map<DownloadKey, DownloadStateDomainModel>>(
        emptyMap(),
    )

    /**
     * Tracks active download jobs so they can be cancelled.
     * Protected by [activeJobsMutex] for thread-safe access.
     */
    private val activeJobs = mutableMapOf<DownloadKey, Job>()
    private val activeJobsMutex = Mutex()

    override fun observeDownloadState(
        bookUuid: String,
        bookType: BookType,
    ): Flow<DownloadStateDomainModel> {
        val key = DownloadKey(bookUuid, bookType)
        return downloadStates
            .map { states -> states[key] ?: getInitialState(bookUuid, bookType) }
            .distinctUntilChanged()
    }

    override fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadStateDomainModel>> {
        return downloadStates
    }

    override suspend fun startDownload(
        bookUuid: String,
        bookType: BookType,
        filePath: String,
        bookTitle: String,
    ) {
        val key = DownloadKey(bookUuid, bookType)

        // Check if already cached
        if (fileDownloader.isEbookCached(bookUuid, bookType)) {
            updateState(key, DownloadStateDomainModel.Cached)
            return
        }

        // Check if already downloading
        val currentState = downloadStates.value[key]
        if (currentState is DownloadStateDomainModel.Downloading) {
            return
        }

        // Start download
        updateState(key, DownloadStateDomainModel.Downloading(progress = null))

        val job = downloadScope.launch {
            fileDownloader.downloadEbookWithProgress(
                ebookFilePath = filePath,
                bookUuid = bookUuid,
                bookType = bookType,
                onProgress = { bytesDownloaded, totalBytes ->
                    val progress = if (totalBytes != null && totalBytes > 0) {
                        (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else {
                        null
                    }
                    updateState(key, DownloadStateDomainModel.Downloading(progress = progress))
                },
            ).onSuccess {
                updateState(key, DownloadStateDomainModel.Cached)
            }.onFailure { error ->
                updateState(key, DownloadStateDomainModel.Failed(error))
            }
            activeJobsMutex.withLock {
                activeJobs.remove(key)
            }
        }

        activeJobsMutex.withLock {
            activeJobs[key] = job
        }
    }

    override suspend fun cancelDownload(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        activeJobsMutex.withLock {
            activeJobs[key]?.cancel()
            activeJobs.remove(key)
        }
        // Delete partial file to ensure isEbookCached returns false
        fileDownloader.deleteEbookCache(bookUuid, bookType)
        updateState(key, DownloadStateDomainModel.Idle)
    }

    override fun clearError(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        val currentState = downloadStates.value[key]
        if (currentState is DownloadStateDomainModel.Failed) {
            updateState(key, DownloadStateDomainModel.Idle)
        }
    }

    override fun getDownloadState(bookUuid: String, bookType: BookType): DownloadStateDomainModel {
        val key = DownloadKey(bookUuid, bookType)
        return downloadStates.value[key] ?: getInitialState(bookUuid, bookType)
    }

    private fun getInitialState(bookUuid: String, bookType: BookType): DownloadStateDomainModel {
        return if (fileDownloader.isEbookCached(bookUuid, bookType)) {
            DownloadStateDomainModel.Cached
        } else {
            DownloadStateDomainModel.Idle
        }
    }

    override fun deleteCache(bookUuid: String, bookType: BookType): Boolean {
        val key = DownloadKey(bookUuid, bookType)
        val deleted = fileDownloader.deleteEbookCache(bookUuid, bookType)
        if (deleted) {
            updateState(key, DownloadStateDomainModel.Idle)
        }
        return deleted
    }

    private fun updateState(key: DownloadKey, state: DownloadStateDomainModel) {
        downloadStates.update { currentStates ->
            currentStates + (key to state)
        }
    }
}

