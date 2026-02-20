package com.retro99.reader.domain.usecase

import com.github.michaelbull.result.getOrElse
import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.BookProgressInfoDomainModel
import com.retro99.server.api.AuthenticatedRepositoryProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Represents a book identifier with its server.
 */
data class BookIdentifier(
    val bookUuid: String,
    val serverId: String,
)

/**
 * Use case for getting progress and cache information for all books.
 * Returns a map of book UUID to progress info.
 */
@Factory
class GetAllBooksProgressInfoUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
    @Provided private val repositoryProvider: AuthenticatedRepositoryProvider,
) {
    /**
     * Gets progress and cache information for all books that have any progress or cache.
     * Fetches both local and remote positions.
     *
     * @param books List of book identifiers (uuid + serverId) to check
     * @return Map of book UUID to progress info
     */
    suspend operator fun invoke(books: List<BookIdentifier>): Map<String, BookProgressInfoDomainModel> {
        if (books.isEmpty()) return emptyMap()

        // Get all local positions
        val localPositions = readerSettingsRepository.getAllPositions()
            .getOrElse { emptyList() }
            .associateBy { it.bookUuid }

        // Fetch remote positions in parallel
        val remotePositions = coroutineScope {
            books.map { book ->
                async {
                    val readerRepo = repositoryProvider.getReaderRepository(book.serverId)
                    val remotePosition = readerRepo?.getRemotePosition(book.bookUuid)
                        ?.getOrElse { null }
                    book.bookUuid to remotePosition?.totalProgression
                }
            }.awaitAll().toMap()
        }

        // Build progress info for each book
        return books.mapNotNull { book ->
            val bookUuid = book.bookUuid
            val localPosition = localPositions[bookUuid]
            val remoteProgression = remotePositions[bookUuid]
            val isEbookCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.EBOOK)
            val isAudiobookCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.AUDIOBOOK)
            val isReadaloudCached = readerSettingsRepository.isEbookCached(bookUuid, BookType.READALOUD)

            // Only include books that have progress or cache
            val localProgression = localPosition?.totalProgression
            val hasLocalProgress = localProgression != null && localProgression > 0.0
            val hasRemoteProgress = remoteProgression != null && remoteProgression > 0.0
            val hasCached = isEbookCached || isAudiobookCached || isReadaloudCached

            if (hasLocalProgress || hasRemoteProgress || hasCached) {
                bookUuid to BookProgressInfoDomainModel(
                    bookUuid = bookUuid,
                    localProgression = localPosition?.totalProgression,
                    remoteProgression = remoteProgression,
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

