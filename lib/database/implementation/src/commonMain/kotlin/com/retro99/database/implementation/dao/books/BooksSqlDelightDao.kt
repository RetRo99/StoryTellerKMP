package com.retro99.database.implementation.dao.books

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.retro99.database.implementation.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * SQLDelight DAO for books table operations.
 */
internal class BooksSqlDelightDao(
    private val databaseManager: DatabaseManager,
) {
    private val database get() = databaseManager.getDatabase()
    private val bookQueries get() = database.bookQueries
    private val personQueries get() = database.personQueries
    private val seriesQueries get() = database.seriesQueries
    private val tagQueries get() = database.tagQueries
    private val collectionQueries get() = database.collectionQueries
    private val statusQueries get() = database.statusQueries
    private val bookAuthorQueries get() = database.bookAuthorQueries
    private val bookNarratorQueries get() = database.bookNarratorQueries
    private val bookCreatorQueries get() = database.bookCreatorQueries
    private val bookSeriesQueries get() = database.bookSeriesQueries
    private val bookTagQueries get() = database.bookTagQueries
    private val bookCollectionQueries get() = database.bookCollectionQueries
    private val mediaFileQueries get() = database.mediaFileQueries
    private val readaloudQueries get() = database.readaloudQueries
    private val positionQueries get() = database.positionQueries
    private val bookmarkQueries get() = database.bookmarkQueries

    // ==================== BOOK OPERATIONS ====================

    suspend fun upsertBook(book: BookSqlDelightEntity) {
        withContext(Dispatchers.Default) {
            bookQueries.upsertBook(
                uuid = book.uuid,
                server_id = book.serverId,
                server_type = book.serverType,
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
        return withContext(Dispatchers.Default) {
            bookQueries.getAllBooks().executeAsList().map { row ->
                BookSqlDelightEntity(
                    uuid = row.uuid,
                    serverId = row.server_id,
                    serverType = row.server_type,
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
        return withContext(Dispatchers.Default) {
            bookQueries.getBookByUuid(uuid).executeAsOneOrNull()?.let { row ->
                BookSqlDelightEntity(
                    uuid = row.uuid,
                    serverId = row.server_id,
                    serverType = row.server_type,
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

    suspend fun getBooksByServer(serverId: String): List<BookSqlDelightEntity> {
        return withContext(Dispatchers.Default) {
            bookQueries.getBooksByServer(serverId).executeAsList().map { row ->
                BookSqlDelightEntity(
                    uuid = row.uuid,
                    serverId = row.server_id,
                    serverType = row.server_type,
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

    suspend fun getBookByServerAndUuid(serverId: String, uuid: String): BookSqlDelightEntity? {
        return withContext(Dispatchers.Default) {
            bookQueries.getBookByServerAndUuid(serverId, uuid).executeAsOneOrNull()?.let { row ->
                BookSqlDelightEntity(
                    uuid = row.uuid,
                    serverId = row.server_id,
                    serverType = row.server_type,
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
        withContext(Dispatchers.Default) {
            bookQueries.deleteAllBooks()
        }
    }

    suspend fun deleteBook(uuid: String) {
        withContext(Dispatchers.Default) {
            bookQueries.deleteBook(uuid)
        }
    }

    suspend fun deleteBooksByServer(serverId: String) {
        withContext(Dispatchers.Default) {
            bookQueries.deleteBooksByServer(serverId)
        }
    }

    suspend fun getBooksCount(): Long {
        return withContext(Dispatchers.Default) {
            bookQueries.getBooksCount().executeAsOne()
        }
    }

    suspend fun getBooksCountByServer(serverId: String): Long {
        return withContext(Dispatchers.Default) {
            bookQueries.getBooksCountByServer(serverId).executeAsOne()
        }
    }

    // ==================== PERSON OPERATIONS ====================

    suspend fun upsertPerson(person: PersonSqlDelightEntity) {
        withContext(Dispatchers.Default) {
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
        return withContext(Dispatchers.Default) {
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
        return withContext(Dispatchers.Default) {
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
        return withContext(Dispatchers.Default) {
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
        return withContext(Dispatchers.Default) {
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
        withContext(Dispatchers.Default) {
            bookAuthorQueries.insertBookAuthor(bookUuid, personUuid)
        }
    }

    suspend fun insertBookNarrator(bookUuid: String, personUuid: String) {
        withContext(Dispatchers.Default) {
            bookNarratorQueries.insertBookNarrator(bookUuid, personUuid)
        }
    }

    suspend fun insertBookCreator(bookUuid: String, personUuid: String) {
        withContext(Dispatchers.Default) {
            bookCreatorQueries.insertBookCreator(bookUuid, personUuid)
        }
    }

    suspend fun deleteBookAuthors(bookUuid: String) {
        withContext(Dispatchers.Default) {
            bookAuthorQueries.deleteBookAuthors(bookUuid)
        }
    }

    suspend fun deleteBookNarrators(bookUuid: String) {
        withContext(Dispatchers.Default) {
            bookNarratorQueries.deleteBookNarrators(bookUuid)
        }
    }

    suspend fun deleteBookCreators(bookUuid: String) {
        withContext(Dispatchers.Default) {
            bookCreatorQueries.deleteBookCreators(bookUuid)
        }
    }

    // ==================== SERIES OPERATIONS ====================

    suspend fun upsertSeries(series: SeriesSqlDelightEntity) {
        withContext(Dispatchers.Default) {
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
        return withContext(Dispatchers.Default) {
            bookSeriesQueries.getSeriesByBookUuid(bookUuid).executeAsList().map { row ->
                BookSeriesSqlDelightEntity(
                    bookUuid = bookUuid,
                    seriesUuid = row.uuid,
                    position = row.position,
                )
            }
        }
    }

    suspend fun getSeriesEntityByUuid(uuid: String): SeriesSqlDelightEntity? {
        return withContext(Dispatchers.Default) {
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

    suspend fun insertBookSeries(bookUuid: String, seriesUuid: String, position: Double?) {
        withContext(Dispatchers.Default) {
            bookSeriesQueries.insertBookSeries(bookUuid, seriesUuid, position?.toDouble())
        }
    }

    suspend fun deleteBookSeries(bookUuid: String) {
        withContext(Dispatchers.Default) {
            bookSeriesQueries.deleteBookSeries(bookUuid)
        }
    }

    suspend fun getAllSeries(): List<SeriesSqlDelightEntity> {
        return withContext(Dispatchers.Default) {
            seriesQueries.getAllSeries().executeAsList().map { row ->
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

    suspend fun deleteAllSeries() {
        withContext(Dispatchers.Default) {
            seriesQueries.deleteAllSeries()
        }
    }

    // ==================== TAG OPERATIONS ====================

    suspend fun upsertTag(tag: TagSqlDelightEntity) {
        withContext(Dispatchers.Default) {
            tagQueries.upsertTag(
                uuid = tag.uuid,
                name = tag.name,
                created_at = tag.createdAt,
                updated_at = tag.updatedAt,
            )
        }
    }

    suspend fun getTagsByBookUuid(bookUuid: String): List<TagSqlDelightEntity> {
        return withContext(Dispatchers.Default) {
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
        withContext(Dispatchers.Default) {
            bookTagQueries.insertBookTag(bookUuid, tagUuid)
        }
    }

    suspend fun deleteBookTags(bookUuid: String) {
        withContext(Dispatchers.Default) {
            bookTagQueries.deleteBookTags(bookUuid)
        }
    }

    // ==================== COLLECTION OPERATIONS ====================

    suspend fun upsertCollection(collection: CollectionSqlDelightEntity) {
        withContext(Dispatchers.Default) {
            collectionQueries.upsertCollection(
                uuid = collection.uuid,
                name = collection.name,
                created_at = collection.createdAt,
                updated_at = collection.updatedAt,
            )
        }
    }

    suspend fun getCollectionsByBookUuid(bookUuid: String): List<CollectionSqlDelightEntity> {
        return withContext(Dispatchers.Default) {
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
        withContext(Dispatchers.Default) {
            bookCollectionQueries.insertBookCollection(bookUuid, collectionUuid)
        }
    }

    suspend fun deleteBookCollections(bookUuid: String) {
        withContext(Dispatchers.Default) {
            bookCollectionQueries.deleteBookCollections(bookUuid)
        }
    }

    // ==================== STATUS OPERATIONS ====================

    suspend fun upsertStatus(status: StatusSqlDelightEntity) {
        withContext(Dispatchers.Default) {
            statusQueries.upsertStatus(
                uuid = status.uuid,
                name = status.name,
                created_at = status.createdAt,
                updated_at = status.updatedAt,
            )
        }
    }

    suspend fun getStatusByUuid(uuid: String): StatusSqlDelightEntity? {
        return withContext(Dispatchers.Default) {
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
        withContext(Dispatchers.Default) {
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
        return withContext(Dispatchers.Default) {
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
        withContext(Dispatchers.Default) {
            mediaFileQueries.deleteMediaFilesByBookUuid(bookUuid)
        }
    }

    // ==================== READALOUD OPERATIONS ====================

    suspend fun upsertReadaloud(readaloud: ReadaloudSqlDelightEntity) {
        withContext(Dispatchers.Default) {
            readaloudQueries.upsertReadaloud(
                uuid = readaloud.uuid,
                book_uuid = readaloud.bookUuid,
                filepath = readaloud.filepath,
                missing = readaloud.missing?.toLong(),
                status = readaloud.status,
                current_stage = readaloud.currentStage,
                stage_progress = readaloud.stageProgress,
                queue_position = readaloud.queuePosition?.toLong(),
                restart_pending = if (readaloud.restartPending == true) 1L else 0L,
                created_at = readaloud.createdAt,
                updated_at = readaloud.updatedAt,
            )
        }
    }

    suspend fun getReadaloudByBookUuid(bookUuid: String): ReadaloudSqlDelightEntity? {
        return withContext(Dispatchers.Default) {
            readaloudQueries.getReadaloudByBookUuid(bookUuid).executeAsOneOrNull()?.let { row ->
                ReadaloudSqlDelightEntity(
                    uuid = row.uuid,
                    bookUuid = row.book_uuid,
                    filepath = row.filepath,
                    missing = row.missing?.toInt(),
                    status = row.status,
                    currentStage = row.current_stage,
                    stageProgress = row.stage_progress,
                    queuePosition = row.queue_position?.toInt(),
                    restartPending = row.restart_pending == 1L,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
        }
    }

    suspend fun deleteReadaloudByBookUuid(bookUuid: String) {
        withContext(Dispatchers.Default) {
            readaloudQueries.deleteReadaloudByBookUuid(bookUuid)
        }
    }

    // ==================== POSITION OPERATIONS ====================

    suspend fun upsertPosition(position: PositionSqlDelightEntity) {
        withContext(Dispatchers.Default) {
            positionQueries.upsertPosition(
                book_uuid = position.bookUuid,
                timestamp = position.timestamp,
                created_at = position.createdAt,
                updated_at = position.updatedAt,
                locator_href = position.locatorHref,
                locator_type = position.locatorType,
                locator_title = position.locatorTitle,
                locator_target = position.locatorTarget?.toLong(),
                audio_timestamp_ms = position.audioTimestampMs,
                chapter_index = position.chapterIndex?.toLong(),
                progression = position.progression,
                total_chapters = position.totalChapters?.toLong(),
                total_duration_ms = position.totalDurationMs,
                total_progression = position.totalProgression,
                position = position.position?.toLong(),
            )
        }
    }

    suspend fun getPositionByBookUuid(bookUuid: String): PositionSqlDelightEntity? {
        return withContext(Dispatchers.Default) {
            positionQueries.getPositionByBookUuid(bookUuid)
                .executeAsOneOrNull()?.let { row ->
                    PositionSqlDelightEntity(
                        bookUuid = row.book_uuid,
                        timestamp = row.timestamp,
                        createdAt = row.created_at,
                        updatedAt = row.updated_at,
                        locatorHref = row.locator_href,
                        locatorType = row.locator_type,
                        locatorTitle = row.locator_title,
                        locatorTarget = row.locator_target?.toInt(),
                        audioTimestampMs = row.audio_timestamp_ms,
                        chapterIndex = row.chapter_index?.toInt(),
                        progression = row.progression,
                        totalChapters = row.total_chapters?.toInt(),
                        totalDurationMs = row.total_duration_ms,
                        totalProgression = row.total_progression,
                        position = row.position?.toInt(),
                    )
                }
        }
    }

    suspend fun deletePosition(bookUuid: String) {
        withContext(Dispatchers.Default) {
            positionQueries.deletePosition(bookUuid)
        }
    }

    suspend fun getAllPositions(): List<PositionSqlDelightEntity> {
        return withContext(Dispatchers.Default) {
            positionQueries.getAllPositions().executeAsList().map { row ->
                PositionSqlDelightEntity(
                    bookUuid = row.book_uuid,
                    timestamp = row.timestamp,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                    locatorHref = row.locator_href,
                    locatorType = row.locator_type,
                    locatorTitle = row.locator_title,
                    locatorTarget = row.locator_target?.toInt(),
                    audioTimestampMs = row.audio_timestamp_ms,
                    chapterIndex = row.chapter_index?.toInt(),
                    progression = row.progression,
                    totalChapters = row.total_chapters?.toInt(),
                    totalDurationMs = row.total_duration_ms,
                    totalProgression = row.total_progression,
                    position = row.position?.toInt(),
                )
            }
        }
    }

    /**
     * Observes position changes for a specific book.
     * Emits whenever the position for this book is updated in the database.
     */
    fun observePositionByBookUuid(bookUuid: String): Flow<PositionSqlDelightEntity?> {
        return positionQueries.getPositionByBookUuid(bookUuid)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { row ->
                row?.let {
                    PositionSqlDelightEntity(
                        bookUuid = it.book_uuid,
                        timestamp = it.timestamp,
                        createdAt = it.created_at,
                        updatedAt = it.updated_at,
                        locatorHref = it.locator_href,
                        locatorType = it.locator_type,
                        locatorTitle = it.locator_title,
                        locatorTarget = it.locator_target?.toInt(),
                        audioTimestampMs = it.audio_timestamp_ms,
                        chapterIndex = it.chapter_index?.toInt(),
                        progression = it.progression,
                        totalChapters = it.total_chapters?.toInt(),
                        totalDurationMs = it.total_duration_ms,
                        totalProgression = it.total_progression,
                        position = it.position?.toInt(),
                    )
                }
            }
    }

    /**
     * Observes all position changes.
     * Emits whenever any position is updated in the database.
     */
    fun observeAllPositions(): Flow<List<PositionSqlDelightEntity>> {
        return positionQueries.getAllPositions()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { row ->
                    PositionSqlDelightEntity(
                        bookUuid = row.book_uuid,
                        timestamp = row.timestamp,
                        createdAt = row.created_at,
                        updatedAt = row.updated_at,
                        locatorHref = row.locator_href,
                        locatorType = row.locator_type,
                        locatorTitle = row.locator_title,
                        locatorTarget = row.locator_target?.toInt(),
                        audioTimestampMs = row.audio_timestamp_ms,
                        chapterIndex = row.chapter_index?.toInt(),
                        progression = row.progression,
                        totalChapters = row.total_chapters?.toInt(),
                        totalDurationMs = row.total_duration_ms,
                        totalProgression = row.total_progression,
                        position = row.position?.toInt(),
                    )
                }
            }
    }

    // ==================== TRANSACTION SUPPORT ====================

    suspend fun <T> transaction(block: suspend () -> T): T {
        return withContext(Dispatchers.Default) {
            database.transactionWithResult {
                block()
            }
        }
    }

    suspend fun deleteAllRelatedData() {
        withContext(Dispatchers.Default) {
            database.transaction {
                bookAuthorQueries.deleteAllBookAuthors()
                bookNarratorQueries.deleteAllBookNarrators()
                bookCreatorQueries.deleteAllBookCreators()
                bookSeriesQueries.deleteAllBookSeries()
                bookTagQueries.deleteAllBookTags()
                bookCollectionQueries.deleteAllBookCollections()
                mediaFileQueries.deleteAllMediaFiles()
                readaloudQueries.deleteAllReadalouds()
                positionQueries.deleteAllPositions()
                bookmarkQueries.deleteAllBookmarks()
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
