package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.BookApiModel
import com.retro99.books.data.model.CollectionApiModel
import com.retro99.books.data.model.MediaFileApiModel
import com.retro99.books.data.model.PersonApiModel
import com.retro99.books.data.model.ReadaloudApiModel
import com.retro99.books.data.model.SeriesApiModel
import com.retro99.books.data.model.StatusApiModel
import com.retro99.books.data.model.TagApiModel
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.BooksDatabase
import com.retro99.database.api.books.CollectionEntity
import com.retro99.database.api.books.MediaFileEntity
import com.retro99.database.api.books.PersonEntity
import com.retro99.database.api.books.ReadaloudEntity
import com.retro99.database.api.books.SeriesWithPositionEntity
import com.retro99.database.api.books.StatusEntity
import com.retro99.database.api.books.TagEntity
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [BooksLocalSource::class])
internal class BooksRoomDataSource(
    @Provided private val booksDatabase: BooksDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : BooksLocalSource {

    override suspend fun getBooks(): AppResult<List<BookApiModel>?> {
        return databaseExecutor.executeDatabaseOperation {
            val books = booksDatabase.getAllBooks()
            if (books.isEmpty()) {
                null
            } else {
                books.map { it.toApiModel() }
            }
        }
    }

    override suspend fun getBook(uuid: String): AppResult<BookApiModel?> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.getBookByUuid(uuid)?.toApiModel()
        }
    }

    override suspend fun saveBooks(books: List<BookApiModel>): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            books.forEach { book ->
                booksDatabase.upsertBook(book.toEntity())
            }
        }
    }

    override suspend fun saveBook(book: BookApiModel): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.upsertBook(book.toEntity())
        }
    }

    override suspend fun clearCache(): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.deleteAllRelatedData()
        }
    }

    // ==================== ENTITY CONVERSIONS ====================

    private fun BookApiModel.toEntity(): BookEntity {
        return BookEntityImpl(
            uuid = uuid,
            id = id,
            title = title,
            subtitle = subtitle,
            language = language,
            publicationDate = publicationDate,
            description = description,
            rating = rating,
            suffix = suffix,
            createdAt = createdAt,
            updatedAt = updatedAt,
            authors = authors.map { it.toEntity() },
            narrators = narrators.map { it.toEntity() },
            creators = creators.map { it.toEntity() },
            series = series.map { it.toEntity() },
            tags = tags.map { it.toEntity() },
            collections = collections.map { it.toEntity() },
            status = status?.toEntity(),
            ebook = ebook?.toEntity(uuid, "ebook"),
            audiobook = audiobook?.toEntity(uuid, "audiobook"),
            readaloud = readaloud?.toEntity(uuid),
        )
    }

    private fun PersonApiModel.toEntity(): PersonEntity {
        return PersonEntityImpl(
            uuid = uuid,
            name = name,
            fileAs = fileAs,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun SeriesApiModel.toEntity(): SeriesWithPositionEntity {
        return SeriesWithPositionEntityImpl(
            uuid = uuid,
            name = name,
            featured = featured,
            position = position,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun TagApiModel.toEntity(): TagEntity {
        return TagEntityImpl(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun CollectionApiModel.toEntity(): CollectionEntity {
        return CollectionEntityImpl(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun StatusApiModel.toEntity(): StatusEntity {
        return StatusEntityImpl(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun MediaFileApiModel.toEntity(bookUuid: String, type: String): MediaFileEntity {
        return MediaFileEntityImpl(
            uuid = uuid,
            bookUuid = bookUuid,
            type = type,
            filepath = filepath,
            missing = missing,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun ReadaloudApiModel.toEntity(bookUuid: String): ReadaloudEntity {
        return ReadaloudEntityImpl(
            uuid = uuid,
            bookUuid = bookUuid,
            filepath = filepath,
            missing = missing,
            status = status,
            currentStage = currentStage,
            stageProgress = stageProgress,
            queuePosition = queuePosition,
            restartPending = restartPending,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun PersonEntity.toApiModel(): PersonApiModel {
        return PersonApiModel(
            uuid = uuid,
            name = name,
            fileAs = fileAs,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun TagEntity.toApiModel(): TagApiModel {
        return TagApiModel(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun CollectionEntity.toApiModel(): CollectionApiModel {
        return CollectionApiModel(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun StatusEntity.toApiModel(): StatusApiModel {
        return StatusApiModel(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun MediaFileEntity.toApiModel(): MediaFileApiModel {
        return MediaFileApiModel(
            uuid = uuid,
            filepath = filepath,
            missing = missing,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun ReadaloudEntity.toApiModel(): ReadaloudApiModel {
        return ReadaloudApiModel(
            uuid = uuid,
            filepath = filepath,
            missing = missing,
            status = status,
            currentStage = currentStage,
            stageProgress = stageProgress,
            queuePosition = queuePosition,
            restartPending = restartPending,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun BookEntity.toApiModel(): BookApiModel {
        return BookApiModel(
            uuid = uuid,
            id = id,
            title = title,
            subtitle = subtitle,
            language = language,
            publicationDate = publicationDate,
            description = description,
            rating = rating,
            suffix = suffix,
            createdAt = createdAt,
            updatedAt = updatedAt,
            authors = authors.map { it.toApiModel() },
            narrators = narrators.map { it.toApiModel() },
            creators = creators.map { it.toApiModel() },
            series = series.map { it.toApiModel() },
            tags = tags.map { it.toApiModel() },
            collections = collections.map { it.toApiModel() },
            status = status?.toApiModel(),
            ebook = ebook?.toApiModel(),
            audiobook = audiobook?.toApiModel(),
            readaloud = readaloud?.toApiModel(),
        )
    }

    private fun SeriesWithPositionEntity.toApiModel(): SeriesApiModel {
        return SeriesApiModel(
            uuid = uuid,
            name = name,
            featured = featured,
            position = position,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

// ==================== ENTITY IMPLEMENTATIONS ====================

private data class BookEntityImpl(
    override val uuid: String,
    override val id: Long,
    override val title: String,
    override val subtitle: String?,
    override val language: String?,
    override val publicationDate: String?,
    override val description: String?,
    override val rating: Float?,
    override val suffix: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
    override val authors: List<PersonEntity>,
    override val narrators: List<PersonEntity>,
    override val creators: List<PersonEntity>,
    override val series: List<SeriesWithPositionEntity>,
    override val tags: List<TagEntity>,
    override val collections: List<CollectionEntity>,
    override val status: StatusEntity?,
    override val ebook: MediaFileEntity?,
    override val audiobook: MediaFileEntity?,
    override val readaloud: ReadaloudEntity?,
) : BookEntity

private data class PersonEntityImpl(
    override val uuid: String,
    override val name: String,
    override val fileAs: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : PersonEntity

private data class SeriesWithPositionEntityImpl(
    override val uuid: String,
    override val name: String,
    override val featured: Int?,
    override val position: Int?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : SeriesWithPositionEntity

private data class TagEntityImpl(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : TagEntity

private data class CollectionEntityImpl(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : CollectionEntity

private data class StatusEntityImpl(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : StatusEntity

private data class MediaFileEntityImpl(
    override val uuid: String,
    override val bookUuid: String,
    override val type: String,
    override val filepath: String,
    override val missing: Int?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : MediaFileEntity

private data class ReadaloudEntityImpl(
    override val uuid: String,
    override val bookUuid: String,
    override val filepath: String,
    override val missing: Int?,
    override val status: String?,
    override val currentStage: String?,
    override val stageProgress: Int?,
    override val queuePosition: Int?,
    override val restartPending: Boolean?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : ReadaloudEntity
