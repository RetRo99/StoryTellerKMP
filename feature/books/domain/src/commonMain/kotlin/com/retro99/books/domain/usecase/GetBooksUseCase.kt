package com.retro99.books.domain.usecase

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map as resultMap
import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookDomainModel
import com.retro99.books.domain.model.BookType
import com.retro99.books.domain.model.MediaFileDomainModel
import com.retro99.books.domain.model.PersonDomainModel
import com.retro99.books.domain.model.ReadaloudDomainModel
import com.retro99.books.domain.model.SeriesDomainModel
import com.retro99.books.domain.model.TagDomainModel
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
 * Use case for getting all books from all authenticated servers.
 * Local books are included via LocalBooksRepository (Local server type).
 * Automatically updates when servers are added/removed or auth state changes.
 */
@Factory
class GetBooksUseCase(
    @Provided private val repositoryProvider: AuthenticatedRepositoryProvider,
) {
    private val logger = Logger.withTag("čič")

    operator fun invoke(): Flow<AppResult<List<BookDomainModel>>> {
        return repositoryProvider.observeBooksRepositories()
            .onEach { repositories ->
                logger.d { "Received ${repositories.size} repositories" }
            }
            .flatMapLatest { repositories ->
                val flows = repositories.map { repo ->
                    logger.d { "Getting books from repo: ${repo.serverId}" }
                    repo.getBooks().onEach { result ->
                        logger.d { "Repo ${repo.serverId} books result: $result" }
                    }.mapToFlow { books ->
                        logger.d { "Repo ${repo.serverId} returned ${books.size} books" }
                        books.map { it.toBookDomainModel() }
                    }
                }

                if (flows.isEmpty()) {
                    logger.d { "No repositories, returning empty list" }
                    flowOf(Ok(emptyList()))
                } else {
                    combine(flows) { results ->
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
 * Returns LocalBook if isLocal flag is true, otherwise StorytellerBook.
 */
private fun ServerBook.toBookDomainModel(): BookDomainModel {
    return if (isLocal) {
        // Determine book type from which filepath is set
        val bookType = when {
            ebookFilepath != null -> BookType.EBOOK
            readaloudFilepath != null -> BookType.READALOUD
            audiobookFilepath != null -> BookType.AUDIOBOOK
            else -> BookType.EBOOK
        }
        // Get the file path and size based on book type
        val filePath = ebookFilepath ?: readaloudFilepath ?: audiobookFilepath ?: ""
        val fileSize = ebookFileSize ?: readaloudFileSize ?: audiobookFileSize ?: 0L

        BookDomainModel.LocalBook(
            uuid = uuid,
            serverId = serverId,
            title = title,
            description = description,
            coverUrl = coverUrl,
            author = authors.firstOrNull(),
            filePath = filePath,
            fileSize = fileSize,
            importedAt = createdAt ?: "",
            lastOpenedAt = lastOpenedAt,
            bookType = bookType,
        )
    } else {
        BookDomainModel.StorytellerBook(
            uuid = uuid,
            serverId = serverId,
            title = title,
            description = description,
            coverUrl = coverUrl,
            id = 0L, // Not available in ServerBook
            language = null,
            createdAt = createdAt,
            updatedAt = null,
            publicationDate = null,
            rating = null,
            suffix = null,
            subtitle = null,
            ebookCoverUrl = null,
            audiobookCoverUrl = null,
            authors = authors.map {
                PersonDomainModel(
                    uuid = it, // Use name as uuid for now
                    id = null,
                    name = it,
                    fileAs = null,
                    createdAt = null,
                    updatedAt = null,
                )
            },
            narrators = narrators.map {
                PersonDomainModel(
                    uuid = it,
                    id = null,
                    name = it,
                    fileAs = null,
                    createdAt = null,
                    updatedAt = null,
                )
            },
            creators = emptyList(),
            series = series.map { s ->
                SeriesDomainModel(
                    uuid = s.id ?: s.name, // Use name as fallback uuid when id is null
                    name = s.name,
                    featured = null,
                    position = s.sequence?.toDouble(),
                    createdAt = null,
                    updatedAt = null,
                )
            },
            tags = tags.map { tagName ->
                TagDomainModel(
                    uuid = tagName, // Use name as uuid
                    name = tagName,
                    createdAt = null,
                    updatedAt = null,
                )
            },
            collections = emptyList(),
            status = null,
            ebook = if (hasEbook) MediaFileDomainModel(
                uuid = "$uuid-ebook",
                filepath = ebookFilepath,
                missing = null,
                createdAt = null,
                updatedAt = null,
            ) else null,
            audiobook = if (hasAudiobook) MediaFileDomainModel(
                uuid = "$uuid-audiobook",
                filepath = audiobookFilepath,
                missing = null,
                createdAt = null,
                updatedAt = null,
            ) else null,
            readaloud = if (hasReadaloud) ReadaloudDomainModel(
                uuid = "$uuid-readaloud",
                filepath = readaloudFilepath,
                missing = null,
                status = null,
                currentStage = null,
                stageProgress = null,
                queuePosition = null,
                restartPending = null,
                createdAt = null,
                updatedAt = null,
            ) else null,
        )
    }
}

