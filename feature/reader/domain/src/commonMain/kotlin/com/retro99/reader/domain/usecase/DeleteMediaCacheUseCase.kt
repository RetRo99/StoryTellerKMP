package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.BookDownloadManager
import com.retro99.books.domain.model.BookType
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class DeleteMediaCacheUseCase(
    @Provided private val downloadManager: BookDownloadManager,
) {
    /**
     * Deletes a cached media file and updates the download state to Idle.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of media to delete (EBOOK, AUDIOBOOK, READALOUD)
     * @return True if the file was deleted successfully
     */
    suspend operator fun invoke(bookUuid: String, bookType: BookType): Boolean {
        return downloadManager.deleteCache(bookUuid, bookType)
    }
}

