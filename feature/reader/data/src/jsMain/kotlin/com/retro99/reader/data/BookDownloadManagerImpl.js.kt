package com.retro99.reader.data

import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.BookDownloadManager
import com.retro99.reader.domain.model.DownloadKey
import com.retro99.reader.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [BookDownloadManager::class])
actual class BookDownloadManagerImpl : BookDownloadManager {

    private val states = MutableStateFlow<Map<DownloadKey, DownloadState>>(emptyMap())

    override fun observeDownloadState(bookUuid: String, bookType: BookType): Flow<DownloadState> =
        states.map { it[DownloadKey(bookUuid, bookType)] ?: DownloadState.Idle }

    override fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadState>> = states

    override suspend fun startDownload(
        bookUuid: String,
        bookType: BookType,
        filePath: String,
        bookTitle: String,
        serverId: String,
    ) {
    }

    override suspend fun cancelDownload(bookUuid: String, bookType: BookType) {
    }

    override fun clearError(bookUuid: String, bookType: BookType) {
    }

    override fun getDownloadState(bookUuid: String, bookType: BookType): DownloadState =
        states.value[DownloadKey(bookUuid, bookType)] ?: DownloadState.Idle

    override suspend fun deleteCache(bookUuid: String, bookType: BookType): Boolean = false
}
