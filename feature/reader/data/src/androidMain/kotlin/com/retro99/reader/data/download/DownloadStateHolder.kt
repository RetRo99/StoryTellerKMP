package com.retro99.reader.data.download

import com.retro99.base.result.AppError
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadKey
import com.retro99.reader.domain.model.DownloadStateDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

/**
 * Singleton that holds download state across the app.
 *
 * This is separate from the service so that:
 * 1. State survives service restarts
 * 2. ViewModels can observe state without binding to the service
 * 3. State can be checked synchronously
 */
@Single
class DownloadStateHolder {

    private val downloadStates = MutableStateFlow<Map<DownloadKey, DownloadStateDomainModel>>(
        emptyMap(),
    )

    fun observeDownloadState(
        bookUuid: String,
        bookType: BookType,
    ): Flow<DownloadStateDomainModel> {
        val key = DownloadKey(bookUuid, bookType)
        return downloadStates.map { states ->
            states[key] ?: DownloadStateDomainModel.Idle
        }
    }

    fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadStateDomainModel>> {
        return downloadStates
    }

    fun getDownloadState(bookUuid: String, bookType: BookType): DownloadStateDomainModel {
        val key = DownloadKey(bookUuid, bookType)
        return downloadStates.value[key] ?: DownloadStateDomainModel.Idle
    }

    fun updateProgress(bookUuid: String, bookType: BookType, progress: Float?) {
        val key = DownloadKey(bookUuid, bookType)
        updateState(key, DownloadStateDomainModel.Downloading(progress))
    }

    fun markCached(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        updateState(key, DownloadStateDomainModel.Cached)
    }

    fun markFailed(bookUuid: String, bookType: BookType, error: AppError) {
        val key = DownloadKey(bookUuid, bookType)
        updateState(key, DownloadStateDomainModel.Failed(error))
    }

    fun markIdle(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        updateState(key, DownloadStateDomainModel.Idle)
    }

    fun clearError(bookUuid: String, bookType: BookType) {
        val key = DownloadKey(bookUuid, bookType)
        val currentState = downloadStates.value[key]
        if (currentState is DownloadStateDomainModel.Failed) {
            updateState(key, DownloadStateDomainModel.Idle)
        }
    }

    private fun updateState(key: DownloadKey, state: DownloadStateDomainModel) {
        downloadStates.update { currentStates ->
            currentStates + (key to state)
        }
    }
}

