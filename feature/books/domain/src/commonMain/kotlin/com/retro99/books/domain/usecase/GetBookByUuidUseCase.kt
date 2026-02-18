package com.retro99.books.domain.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.map as resultMap
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookDomainModel
import com.retro99.books.domain.model.BookType
import com.retro99.books.domain.model.MediaFileDomainModel
import com.retro99.books.domain.model.PersonDomainModel
import com.retro99.books.domain.model.ReadaloudDomainModel
import com.retro99.books.domain.model.SeriesDomainModel
import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.ServerBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map as flowMap
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting a specific book by UUID from a specific server.
 * Requires serverId to query the correct server directly.
 */
@Factory
class GetBookByUuidUseCase(
    @Provided private val repositoryProvider: AuthenticatedRepositoryProvider,
) {
    /**
     * Get a book by UUID from a specific server.
     * @param serverId The ID of the server to query
     * @param uuid The UUID of the book
     * @return Flow emitting the book or an error
     */
    operator fun invoke(serverId: String, uuid: String): Flow<AppResult<BookDomainModel>> = flow {
        val repository = repositoryProvider.getBooksRepository(serverId)
        if (repository == null) {
            emit(Err(AppError.NotFoundError("Server not found or not authenticated: $serverId")))
            return@flow
        }

        repository.getBook(uuid)
            .mapResult { serverBook -> serverBook.toBookDomainModel() }
            .collect { result -> emit(result) }
    }

    /**
     * Maps the result inside a Flow using the Result.map function.
     */
    private fun <T, R> Flow<AppResult<T>>.mapResult(
        transform: (T) -> R
    ): Flow<AppResult<R>> = this.flowMap { result ->
        result.resultMap { transform(it) }
    }
}

/**
 * Maps a ServerBook to BookDomainModel.
 * Returns LocalBook if metadata indicates it's a local book, otherwise StorytellerBook.
 */
private fun ServerBook.toBookDomainModel(): BookDomainModel {
    // Check if this is a local book based on metadata
    val isLocal = metadata["isLocal"] == true

    return if (isLocal) {
        BookDomainModel.LocalBook(
            uuid = uuid,
            serverId = serverId,
            title = title,
            description = description,
            coverUrl = coverUrl,
            author = authors.firstOrNull(),
            filePath = metadata["filePath"] as? String ?: "",
            fileSize = (metadata["fileSize"] as? Number)?.toLong() ?: 0L,
            importedAt = metadata["importedAt"] as? String ?: "",
            lastOpenedAt = metadata["lastOpenedAt"] as? String,
            bookType = (metadata["bookType"] as? String)?.let {
                BookType.fromValue(it)
            } ?: BookType.EBOOK,
        )
    } else {
        BookDomainModel.StorytellerBook(
            uuid = uuid,
            serverId = serverId,
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
                PersonDomainModel(
                    uuid = it,
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
            series = series.mapNotNull { s ->
                s.id?.let { id ->
                    SeriesDomainModel(
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
            ebook = if (hasEbook) MediaFileDomainModel(
                uuid = "$uuid-ebook",
                filepath = "ebook",
                missing = null,
                createdAt = null,
                updatedAt = null,
            ) else null,
            audiobook = if (hasAudiobook) MediaFileDomainModel(
                uuid = "$uuid-audiobook",
                filepath = "audiobook",
                missing = null,
                createdAt = null,
                updatedAt = null,
            ) else null,
            readaloud = if (hasReadaloud) ReadaloudDomainModel(
                uuid = "$uuid-readaloud",
                filepath = "readaloud",
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

