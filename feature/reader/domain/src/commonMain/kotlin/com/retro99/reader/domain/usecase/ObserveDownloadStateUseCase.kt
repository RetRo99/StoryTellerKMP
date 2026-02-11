package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.BookDownloadManager
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for observing the download state of a specific media type.
 */
@Factory
class ObserveDownloadStateUseCase(
    @Provided private val downloadManager: BookDownloadManager,
) {
    /**
     * Observes the download state for a specific book and media type.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of media (EBOOK, AUDIOBOOK, READALOUD)
     * @return Flow emitting the current download state
     */
    operator fun invoke(bookUuid: String, bookType: BookType): Flow<DownloadState> {
        return downloadManager.observeDownloadState(bookUuid, bookType)
    }
}

