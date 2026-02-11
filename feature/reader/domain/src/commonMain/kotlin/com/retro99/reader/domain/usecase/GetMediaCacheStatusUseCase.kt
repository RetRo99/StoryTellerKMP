package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.MediaCacheStatusDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetMediaCacheStatusUseCase(
    @Provided private val readerRepository: ReaderRepository,
) {
    /**
     * Checks the cache status for all media types of a book.
     *
     * @param bookUuid The UUID of the book
     * @return Cache status for ebook, audiobook, and readaloud
     */
    suspend operator fun invoke(bookUuid: String): MediaCacheStatusDomainModel {
        return MediaCacheStatusDomainModel(
            isEbookCached = readerRepository.isEbookCached(bookUuid, BookType.EBOOK),
            isAudiobookCached = readerRepository.isEbookCached(bookUuid, BookType.AUDIOBOOK),
            isReadaloudCached = readerRepository.isEbookCached(bookUuid, BookType.READALOUD),
        )
    }
}

