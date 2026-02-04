package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.data.model.PositionApiModel

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

    /**
     * Gets the reading position for a book from the server.
     */
    suspend fun getPosition(bookUuid: String): AppResult<PositionApiModel?>

    /**
     * Updates the reading position for a book on the server.
     */
    suspend fun updatePosition(bookUuid: String, position: PositionApiModel): CompletableResult
}

