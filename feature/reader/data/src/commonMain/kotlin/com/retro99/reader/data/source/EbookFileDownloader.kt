package com.retro99.reader.data.source

import com.retro99.base.result.AppResult

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
     * @return The local file path where the ebook was saved
     */
    suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
    ): AppResult<String>

    /**
     * Gets the local file path for a cached ebook.
     *
     * @param bookUuid The UUID of the book
     * @return The local file path or null if not cached
     */
    fun getCachedEbookPath(bookUuid: String): String?

    /**
     * Checks if an ebook file exists locally.
     *
     * @param bookUuid The UUID of the book
     * @return True if the file exists locally
     */
    fun isEbookCached(bookUuid: String): Boolean
}

