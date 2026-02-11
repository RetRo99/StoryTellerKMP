package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.BookDownloadManager
import com.retro99.reader.domain.model.BookType
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for starting a media download.
 * The download continues in the background even if the user navigates away.
 */
@Factory
class DownloadMediaUseCase(
    @Provided private val downloadManager: BookDownloadManager,
) {
    /**
     * Starts downloading the specified media type for a book.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of media to download (EBOOK, AUDIOBOOK, READALOUD)
     * @param filePath The file path on the server
     */
    suspend operator fun invoke(bookUuid: String, bookType: BookType, filePath: String) {
        downloadManager.startDownload(bookUuid, bookType, filePath)
    }
}

