package com.retro99.database.api.books

import com.retro99.database.api.DataClearable

/**
 * Database interface for book-related operations.
 * Supports normalized schema with related entities.
 */
interface BooksDatabase : PositionDatabase, DataClearable {

    // ==================== BOOK OPERATIONS ====================

    suspend fun upsertBook(book: BookEntity)

    suspend fun getAllBooks(): List<BookEntity>

    suspend fun getBookByUuid(uuid: String): BookEntity?

    /**
     * Get all books for a specific server.
     */
    suspend fun getBooksByServer(serverId: String): List<BookEntity>

    /**
     * Get a specific book by UUID and server ID.
     * More precise than getBookByUuid when the same UUID might exist on multiple servers.
     */
    suspend fun getBookByServerAndUuid(serverId: String, uuid: String): BookEntity?

    suspend fun deleteAllBooks()

    suspend fun deleteBook(uuid: String)

    /**
     * Delete all books for a specific server.
     */
    suspend fun deleteBooksByServer(serverId: String)

    suspend fun getBooksCount(): Int

    /**
     * Get book count for a specific server.
     */
    suspend fun getBooksCountByServer(serverId: String): Int

    // ==================== PERSON OPERATIONS ====================

    suspend fun upsertPerson(person: PersonEntity)

    suspend fun getAuthorsByBookUuid(bookUuid: String): List<PersonEntity>

    suspend fun getNarratorsByBookUuid(bookUuid: String): List<PersonEntity>

    suspend fun getCreatorsByBookUuid(bookUuid: String): List<PersonEntity>

    suspend fun insertBookAuthor(bookUuid: String, personUuid: String)

    suspend fun insertBookNarrator(bookUuid: String, personUuid: String)

    suspend fun insertBookCreator(bookUuid: String, personUuid: String)

    suspend fun deleteBookAuthors(bookUuid: String)

    suspend fun deleteBookNarrators(bookUuid: String)

    suspend fun deleteBookCreators(bookUuid: String)

    // ==================== SERIES OPERATIONS ====================

    suspend fun upsertSeries(series: SeriesEntity)

    suspend fun getAllSeries(): List<SeriesEntity>

    suspend fun getSeriesByBookUuid(bookUuid: String): List<BookSeriesEntity>

    suspend fun getSeriesEntityByUuid(uuid: String): SeriesEntity?

    suspend fun insertBookSeries(bookUuid: String, seriesUuid: String, position: Double?)

    suspend fun deleteBookSeries(bookUuid: String)

    suspend fun deleteAllSeries()

    // ==================== TAG OPERATIONS ====================

    suspend fun upsertTag(tag: TagEntity)

    suspend fun getTagsByBookUuid(bookUuid: String): List<TagEntity>

    suspend fun insertBookTag(bookUuid: String, tagUuid: String)

    suspend fun deleteBookTags(bookUuid: String)

    // ==================== COLLECTION OPERATIONS ====================

    suspend fun upsertCollection(collection: CollectionEntity)

    suspend fun getCollectionsByBookUuid(bookUuid: String): List<CollectionEntity>

    suspend fun insertBookCollection(bookUuid: String, collectionUuid: String)

    suspend fun deleteBookCollections(bookUuid: String)

    // ==================== STATUS OPERATIONS ====================

    suspend fun upsertStatus(status: StatusEntity)

    suspend fun getStatusByUuid(uuid: String): StatusEntity?

    // ==================== MEDIA FILE OPERATIONS ====================

    suspend fun upsertMediaFile(mediaFile: MediaFileEntity)

    suspend fun getMediaFilesByBookUuid(bookUuid: String): List<MediaFileEntity>

    suspend fun deleteMediaFilesByBookUuid(bookUuid: String)

    // ==================== READALOUD OPERATIONS ====================

    suspend fun upsertReadaloud(readaloud: ReadaloudEntity)

    suspend fun getReadaloudByBookUuid(bookUuid: String): ReadaloudEntity?

    suspend fun deleteReadaloudByBookUuid(bookUuid: String)

    // ==================== TRANSACTION SUPPORT ====================

    override suspend fun clearAllData()
}
