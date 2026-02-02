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
import com.retro99.database.api.books.SeriesEntity
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
                books.map { bookEntity ->
                    loadBookWithRelations(bookEntity)
                }
            }
        }
    }

    override suspend fun getBook(uuid: String): AppResult<BookApiModel?> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.getBookByUuid(uuid)?.let { bookEntity ->
                loadBookWithRelations(bookEntity)
            }
        }
    }

    override suspend fun saveBooks(books: List<BookApiModel>): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            books.forEach { book ->
                saveBookWithRelations(book)
            }
        }
    }

    override suspend fun saveBook(book: BookApiModel): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            saveBookWithRelations(book)
        }
    }

    override suspend fun clearCache(): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.deleteAllRelatedData()
        }
    }

    private suspend fun loadBookWithRelations(bookEntity: BookEntity): BookApiModel {
        val authors = booksDatabase.getAuthorsByBookUuid(bookEntity.uuid)
        val narrators = booksDatabase.getNarratorsByBookUuid(bookEntity.uuid)
        val creators = booksDatabase.getCreatorsByBookUuid(bookEntity.uuid)
        val seriesRelations = booksDatabase.getSeriesByBookUuid(bookEntity.uuid)
        val tags = booksDatabase.getTagsByBookUuid(bookEntity.uuid)
        val collections = booksDatabase.getCollectionsByBookUuid(bookEntity.uuid)
        val status = bookEntity.statusUuid?.let { booksDatabase.getStatusByUuid(it) }
        val mediaFiles = booksDatabase.getMediaFilesByBookUuid(bookEntity.uuid)
        val readaloud = booksDatabase.getReadaloudByBookUuid(bookEntity.uuid)

        // Load series entities with positions
        val seriesList = seriesRelations.mapNotNull { relation ->
            booksDatabase.getSeriesEntityByUuid(relation.seriesUuid)?.let { series ->
                SeriesApiModel(
                    uuid = series.uuid,
                    name = series.name,
                    featured = series.featured,
                    position = relation.position,
                    createdAt = series.createdAt,
                    updatedAt = series.updatedAt,
                )
            }
        }

        return BookApiModel(
            uuid = bookEntity.uuid,
            id = bookEntity.id,
            title = bookEntity.title,
            subtitle = bookEntity.subtitle,
            language = bookEntity.language,
            publicationDate = bookEntity.publicationDate,
            description = bookEntity.description,
            rating = bookEntity.rating,
            suffix = bookEntity.suffix,
            createdAt = bookEntity.createdAt,
            updatedAt = bookEntity.updatedAt,
            authors = authors.map { it.toApiModel() },
            narrators = narrators.map { it.toApiModel() },
            creators = creators.map { it.toApiModel() },
            series = seriesList,
            tags = tags.map { it.toApiModel() },
            collections = collections.map { it.toApiModel() },
            status = status?.toApiModel(),
            ebook = mediaFiles.find { it.type == "ebook" }?.toApiModel(),
            audiobook = mediaFiles.find { it.type == "audiobook" }?.toApiModel(),
            readaloud = readaloud?.toApiModel(),
        )
    }

    private suspend fun saveBookWithRelations(book: BookApiModel) {
        // Save status first if present
        book.status?.let { status ->
            booksDatabase.upsertStatus(status.toEntity())
        }

        // Save the book
        booksDatabase.upsertBook(book.toEntity())

        // Clear existing relations
        booksDatabase.deleteBookAuthors(book.uuid)
        booksDatabase.deleteBookNarrators(book.uuid)
        booksDatabase.deleteBookCreators(book.uuid)
        booksDatabase.deleteBookSeries(book.uuid)
        booksDatabase.deleteBookTags(book.uuid)
        booksDatabase.deleteBookCollections(book.uuid)
        booksDatabase.deleteMediaFilesByBookUuid(book.uuid)
        booksDatabase.deleteReadaloudByBookUuid(book.uuid)

        // Save authors and relations
        book.authors.forEach { author ->
            booksDatabase.upsertPerson(author.toEntity())
            booksDatabase.insertBookAuthor(book.uuid, author.uuid)
        }

        // Save narrators and relations
        book.narrators.forEach { narrator ->
            booksDatabase.upsertPerson(narrator.toEntity())
            booksDatabase.insertBookNarrator(book.uuid, narrator.uuid)
        }

        // Save creators and relations
        book.creators.forEach { creator ->
            booksDatabase.upsertPerson(creator.toEntity())
            booksDatabase.insertBookCreator(book.uuid, creator.uuid)
        }

        // Save series and relations
        book.series.forEach { series ->
            booksDatabase.upsertSeries(series.toEntity())
            booksDatabase.insertBookSeries(book.uuid, series.uuid, series.position)
        }

        // Save tags and relations
        book.tags.forEach { tag ->
            booksDatabase.upsertTag(tag.toEntity())
            booksDatabase.insertBookTag(book.uuid, tag.uuid)
        }

        // Save collections and relations
        book.collections.forEach { collection ->
            booksDatabase.upsertCollection(collection.toEntity())
            booksDatabase.insertBookCollection(book.uuid, collection.uuid)
        }

        // Save media files
        book.ebook?.let { ebook ->
            booksDatabase.upsertMediaFile(ebook.toEntity(book.uuid, "ebook"))
        }
        book.audiobook?.let { audiobook ->
            booksDatabase.upsertMediaFile(audiobook.toEntity(book.uuid, "audiobook"))
        }

        // Save readaloud
        book.readaloud?.let { readaloud ->
            booksDatabase.upsertReadaloud(readaloud.toEntity(book.uuid))
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
            statusUuid = status?.uuid,
            createdAt = createdAt,
            updatedAt = updatedAt,
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

    private fun SeriesApiModel.toEntity(): SeriesEntity {
        return SeriesEntityImpl(
            uuid = uuid,
            name = name,
            featured = featured,
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
    override val statusUuid: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : BookEntity

private data class PersonEntityImpl(
    override val uuid: String,
    override val name: String,
    override val fileAs: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : PersonEntity

private data class SeriesEntityImpl(
    override val uuid: String,
    override val name: String,
    override val featured: Int?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : SeriesEntity

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
