package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.model.MediaCacheStatusDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetMediaCacheStatusUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    /**
     * Checks the cache status for all media types of a book.
     *
     * @param bookUuid The UUID of the book
     * @return Cache status for ebook, audiobook, and readaloud
     */
    suspend operator fun invoke(bookUuid: String): MediaCacheStatusDomainModel {
        return MediaCacheStatusDomainModel(
            isEbookCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.EBOOK),
            isAudiobookCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.AUDIOBOOK),
            isReadaloudCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.READALOUD),
        )
    }
}

