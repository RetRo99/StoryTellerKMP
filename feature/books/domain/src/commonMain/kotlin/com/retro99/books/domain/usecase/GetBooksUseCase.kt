package com.retro99.books.domain.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppResult
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.ServerBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    operator fun invoke(): Flow<AppResult<List<BookDomainModel>>> {
        return repositoryProvider.observeBooksRepositories()
            .flatMapLatest { serverRepositories ->
                // Combine server repositories with local repositories
                val serverFlows = serverRepositories.map { repo ->
                    repo.getBooks().mapToFlow { books ->
                        books.map { it.toBookDomainModel() }
                    }
                }
                val localFlows = localRepositories.map { it.getBooks() }
                val allFlows = serverFlows + localFlows

                if (allFlows.isEmpty()) {
                    flowOf(Ok(emptyList()))
                } else {
                    combine(allFlows) { results ->
                        val allBooks = results.flatMap { it.getOrElse { emptyList() } }
                        Ok(allBooks.sortedBy { it.title.lowercase() })
                    }
                }
            }
    }

    private fun <T, R> Flow<AppResult<T>>.mapToFlow(
        transform: (T) -> R
    ): Flow<AppResult<R>> = kotlinx.coroutines.flow.map { result ->
        result.map(transform)
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
        series = series.map {
            com.retro99.books.domain.model.SeriesDomainModel(
                uuid = it.id,
                name = it.name,
                featured = null,
                position = it.sequence?.toDouble(),
                createdAt = null,
                updatedAt = null,
            )
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

