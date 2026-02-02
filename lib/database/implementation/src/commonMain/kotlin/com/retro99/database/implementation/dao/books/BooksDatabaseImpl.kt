package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.BookSeriesEntity
import com.retro99.database.api.books.BooksDatabase
import com.retro99.database.api.books.CollectionEntity
import com.retro99.database.api.books.MediaFileEntity
import com.retro99.database.api.books.PersonEntity
import com.retro99.database.api.books.ReadaloudEntity
import com.retro99.database.api.books.ReadingProgressEntity
import com.retro99.database.api.books.SeriesEntity
import com.retro99.database.api.books.StatusEntity
import com.retro99.database.api.books.TagEntity

internal class BooksDatabaseImpl(
    private val sqlDelightDao: BooksSqlDelightDao,
) : BooksDatabase {

    // ==================== BOOK OPERATIONS ====================

    override suspend fun upsertBook(book: BookEntity) {
        sqlDelightDao.upsertBook(book.toSqlDelightEntity())
    }

    override suspend fun getAllBooks(): List<BookEntity> {
        return sqlDelightDao.getAllBooks()
    }

    override suspend fun getBookByUuid(uuid: String): BookEntity? {
        return sqlDelightDao.getBookByUuid(uuid)
    }

    override suspend fun deleteAllBooks() {
        sqlDelightDao.deleteAllBooks()
    }

    override suspend fun deleteBook(uuid: String) {
        sqlDelightDao.deleteBook(uuid)
    }

    override suspend fun getBooksCount(): Int {
        return sqlDelightDao.getBooksCount().toInt()
    }

    // ==================== PERSON OPERATIONS ====================

    override suspend fun upsertPerson(person: PersonEntity) {
        sqlDelightDao.upsertPerson(person.toSqlDelightEntity())
    }

    override suspend fun getAuthorsByBookUuid(bookUuid: String): List<PersonEntity> {
        return sqlDelightDao.getAuthorsByBookUuid(bookUuid)
    }

    override suspend fun getNarratorsByBookUuid(bookUuid: String): List<PersonEntity> {
        return sqlDelightDao.getNarratorsByBookUuid(bookUuid)
    }

    override suspend fun getCreatorsByBookUuid(bookUuid: String): List<PersonEntity> {
        return sqlDelightDao.getCreatorsByBookUuid(bookUuid)
    }

    override suspend fun insertBookAuthor(bookUuid: String, personUuid: String) {
        sqlDelightDao.insertBookAuthor(bookUuid, personUuid)
    }

    override suspend fun insertBookNarrator(bookUuid: String, personUuid: String) {
        sqlDelightDao.insertBookNarrator(bookUuid, personUuid)
    }

    override suspend fun insertBookCreator(bookUuid: String, personUuid: String) {
        sqlDelightDao.insertBookCreator(bookUuid, personUuid)
    }

    override suspend fun deleteBookAuthors(bookUuid: String) {
        sqlDelightDao.deleteBookAuthors(bookUuid)
    }

    override suspend fun deleteBookNarrators(bookUuid: String) {
        sqlDelightDao.deleteBookNarrators(bookUuid)
    }

    override suspend fun deleteBookCreators(bookUuid: String) {
        sqlDelightDao.deleteBookCreators(bookUuid)
    }

    // ==================== SERIES OPERATIONS ====================

    override suspend fun upsertSeries(series: SeriesEntity) {
        sqlDelightDao.upsertSeries(series.toSqlDelightEntity())
    }

    override suspend fun getSeriesByBookUuid(bookUuid: String): List<BookSeriesEntity> {
        return sqlDelightDao.getSeriesByBookUuid(bookUuid)
    }

    override suspend fun getSeriesEntityByUuid(uuid: String): SeriesEntity? {
        return sqlDelightDao.getSeriesEntityByUuid(uuid)
    }

    override suspend fun insertBookSeries(bookUuid: String, seriesUuid: String, position: Int?) {
        sqlDelightDao.insertBookSeries(bookUuid, seriesUuid, position)
    }

    override suspend fun deleteBookSeries(bookUuid: String) {
        sqlDelightDao.deleteBookSeries(bookUuid)
    }

    // ==================== TAG OPERATIONS ====================

    override suspend fun upsertTag(tag: TagEntity) {
        sqlDelightDao.upsertTag(tag.toSqlDelightEntity())
    }

    override suspend fun getTagsByBookUuid(bookUuid: String): List<TagEntity> {
        return sqlDelightDao.getTagsByBookUuid(bookUuid)
    }

    override suspend fun insertBookTag(bookUuid: String, tagUuid: String) {
        sqlDelightDao.insertBookTag(bookUuid, tagUuid)
    }

    override suspend fun deleteBookTags(bookUuid: String) {
        sqlDelightDao.deleteBookTags(bookUuid)
    }

    // ==================== COLLECTION OPERATIONS ====================

    override suspend fun upsertCollection(collection: CollectionEntity) {
        sqlDelightDao.upsertCollection(collection.toSqlDelightEntity())
    }

    override suspend fun getCollectionsByBookUuid(bookUuid: String): List<CollectionEntity> {
        return sqlDelightDao.getCollectionsByBookUuid(bookUuid)
    }

    override suspend fun insertBookCollection(bookUuid: String, collectionUuid: String) {
        sqlDelightDao.insertBookCollection(bookUuid, collectionUuid)
    }

    override suspend fun deleteBookCollections(bookUuid: String) {
        sqlDelightDao.deleteBookCollections(bookUuid)
    }

    // ==================== STATUS OPERATIONS ====================

    override suspend fun upsertStatus(status: StatusEntity) {
        sqlDelightDao.upsertStatus(status.toSqlDelightEntity())
    }

    override suspend fun getStatusByUuid(uuid: String): StatusEntity? {
        return sqlDelightDao.getStatusByUuid(uuid)
    }

    // ==================== MEDIA FILE OPERATIONS ====================

    override suspend fun upsertMediaFile(mediaFile: MediaFileEntity) {
        sqlDelightDao.upsertMediaFile(mediaFile.toSqlDelightEntity())
    }

    override suspend fun getMediaFilesByBookUuid(bookUuid: String): List<MediaFileEntity> {
        return sqlDelightDao.getMediaFilesByBookUuid(bookUuid)
    }

    override suspend fun deleteMediaFilesByBookUuid(bookUuid: String) {
        sqlDelightDao.deleteMediaFilesByBookUuid(bookUuid)
    }

    // ==================== READALOUD OPERATIONS ====================

    override suspend fun upsertReadaloud(readaloud: ReadaloudEntity) {
        sqlDelightDao.upsertReadaloud(readaloud.toSqlDelightEntity())
    }

    override suspend fun getReadaloudByBookUuid(bookUuid: String): ReadaloudEntity? {
        return sqlDelightDao.getReadaloudByBookUuid(bookUuid)
    }

    override suspend fun deleteReadaloudByBookUuid(bookUuid: String) {
        sqlDelightDao.deleteReadaloudByBookUuid(bookUuid)
    }

    // ==================== READING PROGRESS OPERATIONS ====================

    override suspend fun upsertReadingProgress(progress: ReadingProgressEntity) {
        sqlDelightDao.upsertReadingProgress(progress.toSqlDelightEntity())
    }

    override suspend fun getReadingProgressByBookUuid(bookUuid: String): ReadingProgressEntity? {
        return sqlDelightDao.getReadingProgressByBookUuid(bookUuid)
    }

    override suspend fun deleteReadingProgress(bookUuid: String) {
        sqlDelightDao.deleteReadingProgress(bookUuid)
    }

    // ==================== TRANSACTION SUPPORT ====================

    override suspend fun deleteAllRelatedData() {
        sqlDelightDao.deleteAllRelatedData()
    }

    // ==================== ENTITY CONVERSIONS ====================

    private fun BookEntity.toSqlDelightEntity(): BookSqlDelightEntity {
        return BookSqlDelightEntity(
            uuid = uuid,
            id = id,
            title = title,
            subtitle = subtitle,
            language = language,
            publicationDate = publicationDate,
            description = description,
            rating = rating,
            suffix = suffix,
            statusUuid = statusUuid,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun PersonEntity.toSqlDelightEntity(): PersonSqlDelightEntity {
        return PersonSqlDelightEntity(
            uuid = uuid,
            name = name,
            fileAs = fileAs,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun SeriesEntity.toSqlDelightEntity(): SeriesSqlDelightEntity {
        return SeriesSqlDelightEntity(
            uuid = uuid,
            name = name,
            featured = featured,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun TagEntity.toSqlDelightEntity(): TagSqlDelightEntity {
        return TagSqlDelightEntity(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun CollectionEntity.toSqlDelightEntity(): CollectionSqlDelightEntity {
        return CollectionSqlDelightEntity(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun StatusEntity.toSqlDelightEntity(): StatusSqlDelightEntity {
        return StatusSqlDelightEntity(
            uuid = uuid,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun MediaFileEntity.toSqlDelightEntity(): MediaFileSqlDelightEntity {
        return MediaFileSqlDelightEntity(
            uuid = uuid,
            bookUuid = bookUuid,
            type = type,
            filepath = filepath,
            missing = missing,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun ReadaloudEntity.toSqlDelightEntity(): ReadaloudSqlDelightEntity {
        return ReadaloudSqlDelightEntity(
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

    private fun ReadingProgressEntity.toSqlDelightEntity(): ReadingProgressSqlDelightEntity {
        return ReadingProgressSqlDelightEntity(
            bookUuid = bookUuid,
            locatorHref = locatorHref,
            locatorType = locatorType,
            locatorTitle = locatorTitle,
            progression = progression,
            totalProgression = totalProgression,
            chapterIndex = chapterIndex,
            totalChapters = totalChapters,
            audioTimestampMs = audioTimestampMs,
            lastReadAt = lastReadAt,
        )
    }
}
