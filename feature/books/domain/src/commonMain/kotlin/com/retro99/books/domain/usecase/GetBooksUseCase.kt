package com.retro99.books.domain.usecase

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map as resultMap
import com.retro99.base.result.AppResult
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.ServerBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map as flowMap
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting all books from all authenticated servers plus local imported books.
 * Automatically updates when servers are added/removed or auth state changes.
 */
@Factory
class GetBooksUseCase(
    @Provided private val repositoryProvider: AuthenticatedRepositoryProvider,
    @Provided private val localRepositories: List<BooksRepository>, // Local/imported books
) {
    private val logger = Logger.withTag("čič-GetBooksUseCase")

    operator fun invoke(): Flow<AppResult<List<BookDomainModel>>> {
        logger.d { "GetBooksUseCase invoked, localRepositories count: ${localRepositories.size}" }
        return repositoryProvider.observeBooksRepositories()
            .onEach { serverRepositories ->
                logger.d { "Received ${serverRepositories.size} server repositories" }
            }
            .flatMapLatest { serverRepositories ->
                // Combine server repositories with local repositories
                val serverFlows = serverRepositories.map { repo ->
                    logger.d { "Getting books from server repo: ${repo.serverId}" }
                    repo.getBooks().onEach { result ->
                        logger.d { "Server ${repo.serverId} books result: $result" }
                    }.mapToFlow { books ->
                        logger.d { "Server ${repo.serverId} returned ${books.size} books" }
                        books.map { it.toBookDomainModel() }
                    }
                }
                val localFlows = localRepositories.map { it.getBooks() }
                val allFlows = serverFlows + localFlows

                logger.d { "Total flows: ${allFlows.size} (${serverFlows.size} server + ${localFlows.size} local)" }

                if (allFlows.isEmpty()) {
                    logger.d { "No flows, returning empty list" }
                    flowOf(Ok(emptyList()))
                } else {
                    combine(allFlows) { results ->
                        val allBooks = results.flatMap { it.getOrElse { emptyList() } }
                        logger.d { "Combined ${allBooks.size} total books from ${results.size} sources" }
                        Ok(allBooks.sortedBy { it.title.lowercase() })
                    }
                }
            }
    }

    private fun <T, R> Flow<AppResult<T>>.mapToFlow(
        transform: (T) -> R
    ): Flow<AppResult<R>> = this.flowMap { result: AppResult<T> ->
        result.resultMap { value: T -> transform(value) }
    }
}

/**
 * Maps a ServerBook to BookDomainModel.
 * This creates a simplified StorytellerBook representation.
 */
private fun ServerBook.toBookDomainModel(): BookDomainModel {
    return BookDomainModel.StorytellerBook(
        uuid = uuid,
        title = title,
        description = description,
        coverUrl = coverUrl,
        id = 0L, // Not available in ServerBook
        language = null,
        createdAt = null,
        updatedAt = null,
        publicationDate = null,
        rating = null,
        suffix = null,
        subtitle = null,
        ebookCoverUrl = null,
        audiobookCoverUrl = null,
        authors = authors.map {
            com.retro99.books.domain.model.PersonDomainModel(
                uuid = it, // Use name as uuid for now
                id = null,
                name = it,
                fileAs = null,
                createdAt = null,
                updatedAt = null,
            )
        },
        narrators = narrators.map {
            com.retro99.books.domain.model.PersonDomainModel(
                uuid = it,
                id = null,
                name = it,
                fileAs = null,
                createdAt = null,
                updatedAt = null,
            )
        },
        creators = emptyList(),
        series = series.mapNotNull { s ->
            s.id?.let { id ->
                com.retro99.books.domain.model.SeriesDomainModel(
                    uuid = id,
                    name = s.name,
                    featured = null,
                    position = s.sequence?.toDouble(),
                    createdAt = null,
                    updatedAt = null,
                )
            }
        },
        tags = emptyList(),
        collections = emptyList(),
        status = null,
        ebook = if (hasEbook) com.retro99.books.domain.model.MediaFileDomainModel(
            uuid = "$uuid-ebook",
            filepath = "ebook",
            missing = null,
            createdAt = null,
            updatedAt = null,
        ) else null,
        audiobook = if (hasAudiobook) com.retro99.books.domain.model.MediaFileDomainModel(
            uuid = "$uuid-audiobook",
            filepath = "audiobook",
            missing = null,
            createdAt = null,
            updatedAt = null,
        ) else null,
        readaloud = null,
    )
}

