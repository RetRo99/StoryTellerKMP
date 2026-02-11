package com.retro99.reader.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.data.model.PositionApiModel

/**
 * Remote source interface for reader-related API operations.
 *
 * Note: Ebook downloads are handled by [BookDownloadManager] which uses
 * [EbookFileDownloader] directly. This source only handles position sync.
 */
interface ReaderRemoteSource {

    /**
     * Gets the reading position for a book from the server.
     */
    suspend fun getPosition(bookUuid: String): AppResult<PositionApiModel?>

    /**
     * Updates the reading position for a book on the server.
     */
    suspend fun updatePosition(bookUuid: String, position: PositionApiModel): CompletableResult
}

