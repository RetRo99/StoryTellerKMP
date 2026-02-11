package com.retro99.reader.data.download

import com.retro99.base.result.AppError
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadKey
import com.retro99.reader.domain.model.DownloadStateDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

/**
 * Singleton that holds download state across the app.
 *
 * This is separate from platform-specific download mechanisms so that:
 * 1. State survives service/process restarts
 * 2. ViewModels can observe state without binding to platform services
 * 3. State can be checked synchronously
 * 4. State management is shared between Android and iOS
 */
@Single
class DownloadStateHolder {

    private val downloadStates = MutableStateFlow<Map<DownloadKey, DownloadStateDomainModel>>(
        emptyMap(),
    )

    /**
     * Track cancelled downloads to prevent race conditions where progress updates
     * arrive after cancellation. Protected by [cancelledMutex] for thread-safe access.
     */
    private val cancelledDownloads = mutableSetOf<DownloadKey>()
    private val cancelledMutex = Mutex()

    fun observeDownloadState(
        bookUuid: String,
        bookType: BookType,
    ): Flow<DownloadStateDomainModel> {
        val key = DownloadKey(bookUuid, bookType)
        return downloadStates
            .map { states -> states[key] ?: DownloadStateDomainModel.Idle }
            .distinctUntilChanged()
    }

    fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadStateDomainModel>> {
        return downloadStates
    }

    fun getDownloadState(bookUuid: String, bookType: BookType): DownloadStateDomainModel {
        val key = DownloadKey(bookUuid, bookType)
        return downloadStates.value[key] ?: DownloadStateDomainModel.Idle
    }

    suspend fun updateProgress(bookUuid: String, bookType: BookType, progress: Float?) {
        val key = DownloadKey(bookUuid, bookType)
        // Ignore progress updates for cancelled downloads
        val isCancelled = cancelledMutex.withLock { cancelledDownloads.contains(key) }
        if (isCancelled) return
        updateState(key, DownloadStateDomainModel.Downloading(progress))
    }

    suspend fun markCached(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        // Ignore if download was cancelled
        val wasCancelled = cancelledMutex.withLock {
            if (cancelledDownloads.contains(key)) {
                cancelledDownloads.remove(key)
                true
            } else {
                false
            }
        }
        if (wasCancelled) return
        updateState(key, DownloadStateDomainModel.Cached)
    }

    suspend fun markFailed(bookUuid: String, bookType: BookType, error: AppError) {
        val key = DownloadKey(bookUuid, bookType)
        // Ignore if download was cancelled
        val wasCancelled = cancelledMutex.withLock {
            if (cancelledDownloads.contains(key)) {
                cancelledDownloads.remove(key)
                true
            } else {
                false
            }
        }
        if (wasCancelled) return
        updateState(key, DownloadStateDomainModel.Failed(error))
    }

    suspend fun markIdle(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        // Mark as cancelled to prevent race conditions with pending progress updates
        cancelledMutex.withLock { cancelledDownloads.add(key) }
        updateState(key, DownloadStateDomainModel.Idle)
    }

    fun clearError(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        val currentState = downloadStates.value[key]
        if (currentState is DownloadStateDomainModel.Failed) {
            updateState(key, DownloadStateDomainModel.Idle)
        }
    }

    /**
     * Called when starting a new download to clear any previous cancellation state.
     */
    suspend fun clearCancelledState(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        cancelledMutex.withLock { cancelledDownloads.remove(key) }
    }

    private fun updateState(key: DownloadKey, state: DownloadStateDomainModel) {
        downloadStates.update { currentStates ->
            currentStates + (key to state)
        }
    }
}

