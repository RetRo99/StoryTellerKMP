package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookType

/**
 * Platform-specific interface for downloading ebook files.
 * Each platform implements this to handle file system operations.
 */
expect class EbookFileDownloader {

    /**
     * Downloads an ebook file from the server and saves it locally.
     *
     * @param ebookFilePath The file path on the server
     * @param bookUuid The UUID of the book (used for local file naming)
     * @param bookType The type of book (determines the download format query)
     * @return The local file path where the ebook was saved
     */
    suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
        serverId: String,
    ): AppResult<String>

    /**
     * Downloads an ebook file from the server and saves it locally, reporting progress.
     *
     * @param ebookFilePath The file path on the server
     * @param bookUuid The UUID of the book (used for local file naming)
     * @param bookType The type of book (determines the download format query)
     * @param onProgress Suspend callback invoked with bytes downloaded and total bytes (null if unknown)
     * @return The local file path where the ebook was saved
     */
    suspend fun downloadEbookWithProgress(
        ebookFilePath: String,
        bookUuid: String,
        bookType: BookType,
        serverId: String,
        onProgress: suspend (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
    ): AppResult<String>

    /**
     * Gets the local file path for a cached ebook.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     * @return The local file path or null if not cached
     */
    fun getCachedEbookPath(bookUuid: String, bookType: BookType): String?

    /**
     * Checks if an ebook file exists locally.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     * @return True if the file exists locally
     */
    fun isEbookCached(bookUuid: String, bookType: BookType): Boolean

    /**
     * Deletes a cached ebook file.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book
     * @return True if the file was deleted successfully
     */
    fun deleteEbookCache(bookUuid: String, bookType: BookType): Boolean
}

