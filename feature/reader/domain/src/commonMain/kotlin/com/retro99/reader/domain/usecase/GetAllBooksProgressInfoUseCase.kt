package com.retro99.reader.domain.usecase

import com.github.michaelbull.result.getOrElse
import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.BookProgressInfoDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting progress and cache information for all books.
 * Returns a map of book UUID to progress info.
 */
@Factory
class GetAllBooksProgressInfoUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    /**
     * Gets progress and cache information for all books that have any progress or cache.
     *
     * @param bookUuids List of book UUIDs to check cache status for
     * @return Map of book UUID to progress info
     */
    suspend operator fun invoke(bookUuids: List<String>): Map<String, BookProgressInfoDomainModel> {
        // Get all positions from local storage
        val positions = readerSettingsRepository.getAllPositions()
            .getOrElse { emptyList() }
            .associateBy { it.bookUuid }

        // Build progress info for each book
        return bookUuids.mapNotNull { bookUuid ->
            val position = positions[bookUuid]
            val isEbookCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.EBOOK)
            val isAudiobookCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.AUDIOBOOK)
            val isReadaloudCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.READALOUD)

            // Only include books that have progress or cache
            val totalProgression = position?.totalProgression
            val hasProgress = totalProgression != null && totalProgression > 0.0
            val hasCached = isEbookCached || isAudiobookCached || isReadaloudCached

            if (hasProgress || hasCached) {
                bookUuid to BookProgressInfoDomainModel(
                    bookUuid = bookUuid,
                    totalProgression = position?.totalProgression,
                    isEbookCached = isEbookCached,
                    isAudiobookCached = isAudiobookCached,
                    isReadaloudCached = isReadaloudCached,
                )
            } else {
                null
            }
        }.toMap()
    }
}

