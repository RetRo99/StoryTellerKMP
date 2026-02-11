package com.retro99.reader.domain

import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadKey
import com.retro99.reader.domain.model.DownloadStateDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Manages ebook downloads with a lifecycle independent of any ViewModel.
 * Downloads continue even when the user navigates away from the screen.
 *
 * This is a singleton that maintains download state across the app.
 */
interface BookDownloadManager {

    /**
     * Observes the download state for a specific book and type.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book (EBOOK, AUDIOBOOK, READALOUD)
     * @return Flow emitting the current download state
     */
    fun observeDownloadState(bookUuid: String, bookType: BookType): Flow<DownloadStateDomainModel>

    /**
     * Observes all active downloads.
     *
     * @return Flow emitting a map of download keys to their states
     */
    fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadStateDomainModel>>

    /**
     * Starts downloading a book.
     * If the book is already cached or downloading, this is a no-op.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book (EBOOK, AUDIOBOOK, READALOUD)
     * @param filePath The file path on the server
     * @param bookTitle The title of the book (used for notification display)
     */
    suspend fun startDownload(
        bookUuid: String,
        bookType: BookType,
        filePath: String,
        bookTitle: String,
    )

    /**
     * Cancels an ongoing download.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     */
    fun cancelDownload(bookUuid: String, bookType: BookType)

    /**
     * Clears the error state for a failed download, resetting it to Idle.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     */
    fun clearError(bookUuid: String, bookType: BookType)

    /**
     * Gets the current download state synchronously.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     * @return The current download state
     */
    fun getDownloadState(bookUuid: String, bookType: BookType): DownloadStateDomainModel
}

