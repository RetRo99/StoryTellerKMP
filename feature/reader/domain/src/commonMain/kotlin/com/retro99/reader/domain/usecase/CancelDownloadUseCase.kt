package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.BookDownloadManager
import com.retro99.books.domain.model.BookType
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for cancelling an ongoing media download.
 */
@Factory
class CancelDownloadUseCase(
    @Provided private val downloadManager: BookDownloadManager,
) {
    /**
     * Cancels the download for the specified media type.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of media to cancel (EBOOK, AUDIOBOOK, READALOUD)
     */
    suspend operator fun invoke(bookUuid: String, bookType: BookType) {
        downloadManager.cancelDownload(bookUuid, bookType)
    }
}

