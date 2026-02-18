package com.retro99.books.domain.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.ServerBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting a specific book by UUID from any authenticated server or local storage.
 */
@Factory
class GetBookByUuidUseCase(
    @Provided private val repositoryProvider: AuthenticatedRepositoryProvider,
    @Provided private val localRepositories: List<BooksRepository>,
) {
    operator fun invoke(uuid: String): Flow<AppResult<BookDomainModel>> {
        return repositoryProvider.observeBooksRepositories()
            .flatMapLatest { serverRepositories ->
                // Combine server repositories with local repositories
                val serverFlows = serverRepositories.map { repo ->
                    repo.getBook(uuid).map { result ->
                        result.map { it.toBookDomainModel() }
                    }
                }
                val localFlows = localRepositories.map { it.getBook(uuid) }
                val allFlows = serverFlows + localFlows

                if (allFlows.isEmpty()) {
                    flowOf(Err(AppError.NotFoundError("No repositories available")))
                } else {
                    combine(allFlows) { results ->
                        // Return first successful result
                        results.mapNotNull { it.getOrElse { null } }.firstOrNull()
                    }.map { book ->
                        book?.let { Ok(it) }
                            ?: Err(AppError.NotFoundError("Book not found: $uuid"))
                    }
                }
            }
    }
}

/**
 * Maps a ServerBook to BookDomainModel.
 */
private fun ServerBook.toBookDomainModel(): BookDomainModel {
    return BookDomainModel.StorytellerBook(
        uuid = uuid,
        title = title,
        description = description,
        coverUrl = coverUrl,
        id = 0L,
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
                uuid = it,
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

