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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Implementation of [BookDownloadManager] that manages downloads with a lifecycle
 * independent of any ViewModel. Downloads continue even when the user navigates away.
 */
@Single(binds = [BookDownloadManager::class])
class BookDownloadManagerImpl(
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
     */
    private val activeJobs = mutableMapOf<DownloadKey, Job>()

    override fun observeDownloadState(
        bookUuid: String,
        bookType: BookType,
    ): Flow<DownloadStateDomainModel> {
        val key = DownloadKey(bookUuid, bookType)
        return downloadStates.map { states ->
            states[key] ?: getInitialState(bookUuid, bookType)
        }
    }

    override fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadStateDomainModel>> {
        return downloadStates
    }

    override suspend fun startDownload(bookUuid: String, bookType: BookType, filePath: String) {
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
            activeJobs.remove(key)
        }

        activeJobs[key] = job
    }

    override fun cancelDownload(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        activeJobs[key]?.cancel()
        activeJobs.remove(key)
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

    private fun updateState(key: DownloadKey, state: DownloadStateDomainModel) {
        downloadStates.update { currentStates ->
            currentStates + (key to state)
        }
    }
}

