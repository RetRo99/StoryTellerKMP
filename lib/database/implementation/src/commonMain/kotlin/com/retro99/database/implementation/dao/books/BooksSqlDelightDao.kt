package com.retro99.database.implementation.dao.books

import com.retro99.database.implementation.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * SQLDelight DAO for books table operations.
 */
internal class BooksSqlDelightDao(
    private val database: AppDatabase,
) {
    private val bookQueries = database.bookQueries
    private val personQueries = database.personQueries
    private val seriesQueries = database.seriesQueries
    private val tagQueries = database.tagQueries
    private val collectionQueries = database.collectionQueries
    private val statusQueries = database.statusQueries
    private val bookAuthorQueries = database.bookAuthorQueries
    private val bookNarratorQueries = database.bookNarratorQueries
    private val bookCreatorQueries = database.bookCreatorQueries
    private val bookSeriesQueries = database.bookSeriesQueries
    private val bookTagQueries = database.bookTagQueries
    private val bookCollectionQueries = database.bookCollectionQueries
    private val mediaFileQueries = database.mediaFileQueries
    private val readaloudQueries = database.readaloudQueries
    private val readingProgressQueries = database.readingProgressQueries

    // ==================== BOOK OPERATIONS ====================

    suspend fun upsertBook(book: BookSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            bookQueries.upsertBook(
                uuid = book.uuid,
                id = book.id,
                title = book.title,
                subtitle = book.subtitle,
                language = book.language,
                publication_date = book.publicationDate,
                description = book.description,
                rating = book.rating?.toDouble(),
                suffix = book.suffix,
                status_uuid = book.statusUuid,
                created_at = book.createdAt,
                updated_at = book.updatedAt,
            )
        }
    }

    suspend fun getAllBooks(): List<BookSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            bookQueries.getAllBooks().executeAsList().map { row ->
                BookSqlDelightEntity(
                    uuid = row.uuid,
                    id = row.id,
                    title = row.title,
                    subtitle = row.subtitle,
                    language = row.language,
                    publicationDate = row.publication_date,
                    description = row.description,
                    rating = row.rating?.toFloat(),
                    suffix = row.suffix,
                    statusUuid = row.status_uuid,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun getBookByUuid(uuid: String): BookSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            bookQueries.getBookByUuid(uuid).executeAsOneOrNull()?.let { row ->
                BookSqlDelightEntity(
                    uuid = row.uuid,
                    id = row.id,
                    title = row.title,
                    subtitle = row.subtitle,
                    language = row.language,
                    publicationDate = row.publication_date,
                    description = row.description,
                    rating = row.rating?.toFloat(),
                    suffix = row.suffix,
                    statusUuid = row.status_uuid,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun deleteAllBooks() {
        withContext(Dispatchers.IO) {
            bookQueries.deleteAllBooks()
        }
    }

    suspend fun deleteBook(uuid: String) {
        withContext(Dispatchers.IO) {
            bookQueries.deleteBook(uuid)
        }
    }

    suspend fun getBooksCount(): Long {
        return withContext(Dispatchers.IO) {
            bookQueries.getBooksCount().executeAsOne()
        }
    }

    // ==================== PERSON OPERATIONS ====================

    suspend fun upsertPerson(person: PersonSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            personQueries.upsertPerson(
                uuid = person.uuid,
                name = person.name,
                file_as = person.fileAs,
                created_at = person.createdAt,
                updated_at = person.updatedAt,
            )
        }
    }

    suspend fun getPersonByUuid(uuid: String): PersonSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            personQueries.getPersonByUuid(uuid).executeAsOneOrNull()?.let { row ->
                PersonSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    fileAs = row.file_as,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun getAuthorsByBookUuid(bookUuid: String): List<PersonSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            bookAuthorQueries.getAuthorsByBookUuid(bookUuid).executeAsList().map { row ->
                PersonSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    fileAs = row.file_as,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun getNarratorsByBookUuid(bookUuid: String): List<PersonSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            bookNarratorQueries.getNarratorsByBookUuid(bookUuid).executeAsList().map { row ->
                PersonSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    fileAs = row.file_as,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun getCreatorsByBookUuid(bookUuid: String): List<PersonSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            bookCreatorQueries.getCreatorsByBookUuid(bookUuid).executeAsList().map { row ->
                PersonSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    fileAs = row.file_as,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun insertBookAuthor(bookUuid: String, personUuid: String) {
        withContext(Dispatchers.IO) {
            bookAuthorQueries.insertBookAuthor(bookUuid, personUuid)
        }
    }

    suspend fun insertBookNarrator(bookUuid: String, personUuid: String) {
        withContext(Dispatchers.IO) {
            bookNarratorQueries.insertBookNarrator(bookUuid, personUuid)
        }
    }

    suspend fun insertBookCreator(bookUuid: String, personUuid: String) {
        withContext(Dispatchers.IO) {
            bookCreatorQueries.insertBookCreator(bookUuid, personUuid)
        }
    }

    suspend fun deleteBookAuthors(bookUuid: String) {
        withContext(Dispatchers.IO) {
            bookAuthorQueries.deleteBookAuthors(bookUuid)
        }
    }

    suspend fun deleteBookNarrators(bookUuid: String) {
        withContext(Dispatchers.IO) {
            bookNarratorQueries.deleteBookNarrators(bookUuid)
        }
    }

    suspend fun deleteBookCreators(bookUuid: String) {
        withContext(Dispatchers.IO) {
            bookCreatorQueries.deleteBookCreators(bookUuid)
        }
    }

    // ==================== SERIES OPERATIONS ====================

    suspend fun upsertSeries(series: SeriesSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            seriesQueries.upsertSeries(
                uuid = series.uuid,
                name = series.name,
                featured = series.featured?.toLong(),
                created_at = series.createdAt,
                updated_at = series.updatedAt,
            )
        }
    }

    suspend fun getSeriesByBookUuid(bookUuid: String): List<BookSeriesSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            bookSeriesQueries.getSeriesByBookUuid(bookUuid).executeAsList().map { row ->
                BookSeriesSqlDelightEntity(
                    bookUuid = bookUuid,
                    seriesUuid = row.uuid,
                    position = row.position?.toInt(),
                )
            }
        }
    }

    suspend fun getSeriesEntityByUuid(uuid: String): SeriesSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            seriesQueries.getSeriesByUuid(uuid).executeAsOneOrNull()?.let { row ->
                SeriesSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    featured = row.featured?.toInt(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun insertBookSeries(bookUuid: String, seriesUuid: String, position: Int?) {
        withContext(Dispatchers.IO) {
            bookSeriesQueries.insertBookSeries(bookUuid, seriesUuid, position?.toLong())
        }
    }

    suspend fun deleteBookSeries(bookUuid: String) {
        withContext(Dispatchers.IO) {
            bookSeriesQueries.deleteBookSeries(bookUuid)
        }
    }

    // ==================== TAG OPERATIONS ====================

    suspend fun upsertTag(tag: TagSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            tagQueries.upsertTag(
                uuid = tag.uuid,
                name = tag.name,
                created_at = tag.createdAt,
                updated_at = tag.updatedAt,
            )
        }
    }

    suspend fun getTagsByBookUuid(bookUuid: String): List<TagSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            bookTagQueries.getTagsByBookUuid(bookUuid).executeAsList().map { row ->
                TagSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun insertBookTag(bookUuid: String, tagUuid: String) {
        withContext(Dispatchers.IO) {
            bookTagQueries.insertBookTag(bookUuid, tagUuid)
        }
    }

    suspend fun deleteBookTags(bookUuid: String) {
        withContext(Dispatchers.IO) {
            bookTagQueries.deleteBookTags(bookUuid)
        }
    }

    // ==================== COLLECTION OPERATIONS ====================

    suspend fun upsertCollection(collection: CollectionSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            collectionQueries.upsertCollection(
                uuid = collection.uuid,
                name = collection.name,
                created_at = collection.createdAt,
                updated_at = collection.updatedAt,
            )
        }
    }

    suspend fun getCollectionsByBookUuid(bookUuid: String): List<CollectionSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            bookCollectionQueries.getCollectionsByBookUuid(bookUuid).executeAsList().map { row ->
                CollectionSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun insertBookCollection(bookUuid: String, collectionUuid: String) {
        withContext(Dispatchers.IO) {
            bookCollectionQueries.insertBookCollection(bookUuid, collectionUuid)
        }
    }

    suspend fun deleteBookCollections(bookUuid: String) {
        withContext(Dispatchers.IO) {
            bookCollectionQueries.deleteBookCollections(bookUuid)
        }
    }

    // ==================== STATUS OPERATIONS ====================

    suspend fun upsertStatus(status: StatusSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            statusQueries.upsertStatus(
                uuid = status.uuid,
                name = status.name,
                created_at = status.createdAt,
                updated_at = status.updatedAt,
            )
        }
    }

    suspend fun getStatusByUuid(uuid: String): StatusSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            statusQueries.getStatusByUuid(uuid).executeAsOneOrNull()?.let { row ->
                StatusSqlDelightEntity(
                    uuid = row.uuid,
                    name = row.name,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    // ==================== MEDIA FILE OPERATIONS ====================

    suspend fun upsertMediaFile(mediaFile: MediaFileSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            mediaFileQueries.upsertMediaFile(
                uuid = mediaFile.uuid,
                book_uuid = mediaFile.bookUuid,
                type = mediaFile.type,
                filepath = mediaFile.filepath,
                missing = mediaFile.missing?.toLong(),
                created_at = mediaFile.createdAt,
                updated_at = mediaFile.updatedAt,
            )
        }
    }

    suspend fun getMediaFilesByBookUuid(bookUuid: String): List<MediaFileSqlDelightEntity> {
        return withContext(Dispatchers.IO) {
            mediaFileQueries.getMediaFilesByBookUuid(bookUuid).executeAsList().map { row ->
                MediaFileSqlDelightEntity(
                    uuid = row.uuid,
                    bookUuid = row.book_uuid,
                    type = row.type,
                    filepath = row.filepath,
                    missing = row.missing?.toInt(),
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun deleteMediaFilesByBookUuid(bookUuid: String) {
        withContext(Dispatchers.IO) {
            mediaFileQueries.deleteMediaFilesByBookUuid(bookUuid)
        }
    }

    // ==================== READALOUD OPERATIONS ====================

    suspend fun upsertReadaloud(readaloud: ReadaloudSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            readaloudQueries.upsertReadaloud(
                uuid = readaloud.uuid,
                book_uuid = readaloud.bookUuid,
                filepath = readaloud.filepath,
                missing = readaloud.missing?.toLong(),
                status = readaloud.status,
                current_stage = readaloud.currentStage,
                stage_progress = readaloud.stageProgress?.toLong(),
                queue_position = readaloud.queuePosition?.toLong(),
                restart_pending = if (readaloud.restartPending == true) 1L else 0L,
                created_at = readaloud.createdAt,
                updated_at = readaloud.updatedAt,
            )
        }
    }

    suspend fun getReadaloudByBookUuid(bookUuid: String): ReadaloudSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            readaloudQueries.getReadaloudByBookUuid(bookUuid).executeAsOneOrNull()?.let { row ->
                ReadaloudSqlDelightEntity(
                    uuid = row.uuid,
                    bookUuid = row.book_uuid,
                    filepath = row.filepath,
                    missing = row.missing?.toInt(),
                    status = row.status,
                    currentStage = row.current_stage,
                    stageProgress = row.stage_progress?.toInt(),
                    queuePosition = row.queue_position?.toInt(),
                    restartPending = row.restart_pending == 1L,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun deleteReadaloudByBookUuid(bookUuid: String) {
        withContext(Dispatchers.IO) {
            readaloudQueries.deleteReadaloudByBookUuid(bookUuid)
        }
    }

    // ==================== READING PROGRESS OPERATIONS ====================

    suspend fun upsertReadingProgress(progress: ReadingProgressSqlDelightEntity) {
        withContext(Dispatchers.IO) {
            readingProgressQueries.upsertReadingProgress(
                book_uuid = progress.bookUuid,
                locator_href = progress.locatorHref,
                locator_type = progress.locatorType,
                locator_title = progress.locatorTitle,
                progression = progress.progression,
                total_progression = progress.totalProgression,
                chapter_index = progress.chapterIndex?.toLong(),
                total_chapters = progress.totalChapters?.toLong(),
                audio_timestamp_ms = progress.audioTimestampMs,
                last_read_at = progress.lastReadAt,
            )
        }
    }

    suspend fun getReadingProgressByBookUuid(bookUuid: String): ReadingProgressSqlDelightEntity? {
        return withContext(Dispatchers.IO) {
            readingProgressQueries.getReadingProgressByBookUuid(bookUuid)
                .executeAsOneOrNull()?.let { row ->
                    ReadingProgressSqlDelightEntity(
                        bookUuid = row.book_uuid,
                        locatorHref = row.locator_href,
                        locatorType = row.locator_type,
                        locatorTitle = row.locator_title,
                        progression = row.progression,
                        totalProgression = row.total_progression,
                        chapterIndex = row.chapter_index?.toInt(),
                        totalChapters = row.total_chapters?.toInt(),
                        audioTimestampMs = row.audio_timestamp_ms,
                        lastReadAt = row.last_read_at,
                    )
                }
        }
    }

    suspend fun deleteReadingProgress(bookUuid: String) {
        withContext(Dispatchers.IO) {
            readingProgressQueries.deleteReadingProgress(bookUuid)
        }
    }

    // ==================== TRANSACTION SUPPORT ====================

    suspend fun <T> transaction(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            database.transactionWithResult {
                kotlinx.coroutines.runBlocking { block() }
            }
        }
    }

    suspend fun deleteAllRelatedData() {
        withContext(Dispatchers.IO) {
            database.transaction {
                bookAuthorQueries.deleteAllBookAuthors()
                bookNarratorQueries.deleteAllBookNarrators()
                bookCreatorQueries.deleteAllBookCreators()
                bookSeriesQueries.deleteAllBookSeries()
                bookTagQueries.deleteAllBookTags()
                bookCollectionQueries.deleteAllBookCollections()
                mediaFileQueries.deleteAllMediaFiles()
                readaloudQueries.deleteAllReadalouds()
                readingProgressQueries.deleteAllReadingProgress()
                personQueries.deleteAllPersons()
                seriesQueries.deleteAllSeries()
                tagQueries.deleteAllTags()
                collectionQueries.deleteAllCollections()
                statusQueries.deleteAllStatuses()
                bookQueries.deleteAllBooks()
            }
        }
    }
}
