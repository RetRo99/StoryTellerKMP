package com.retro99.reader.data.source

import com.retro99.base.result.AppResult

/**
 * Remote source interface for downloading ebook files.
 */
interface ReaderRemoteSource {

    /**
     * Downloads an ebook file from the server.
     *
     * @param ebookFilePath The file path on the server
     * @param bookUuid The UUID of the book (used for local storage)
     * @return The local file path where the ebook was saved
     */
    suspend fun downloadEbook(
        ebookFilePath: String,
        bookUuid: String,
    ): AppResult<String>
}

