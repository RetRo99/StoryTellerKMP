package com.retro99.reader.domain

import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.model.DownloadKey
import com.retro99.reader.domain.model.DownloadState
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
    fun observeDownloadState(bookUuid: String, bookType: BookType): Flow<DownloadState>

    /**
     * Observes all active downloads.
     *
     * @return Flow emitting a map of download keys to their states
     */
    fun observeAllDownloads(): Flow<Map<DownloadKey, DownloadState>>

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
        serverId: String,
    )

    /**
     * Cancels an ongoing download.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     */
    suspend fun cancelDownload(bookUuid: String, bookType: BookType)

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
    fun getDownloadState(bookUuid: String, bookType: BookType): DownloadState

    /**
     * Deletes a cached media file and updates the state to Idle.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     * @return True if the file was deleted successfully
     */
    suspend fun deleteCache(bookUuid: String, bookType: BookType): Boolean
}

