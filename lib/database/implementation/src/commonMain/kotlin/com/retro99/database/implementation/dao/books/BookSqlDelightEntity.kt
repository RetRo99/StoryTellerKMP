package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.BookSeriesEntity
import com.retro99.database.api.books.CollectionEntity
import com.retro99.database.api.books.MediaFileEntity
import com.retro99.database.api.books.PersonEntity
import com.retro99.database.api.books.ReadaloudEntity
import com.retro99.database.api.books.ReadingProgressEntity
import com.retro99.database.api.books.SeriesEntity
import com.retro99.database.api.books.StatusEntity
import com.retro99.database.api.books.TagEntity

/**
 * SQLDelight entity for books table.
 */
data class BookSqlDelightEntity(
    val uuid: String,
    val id: Long,
    val title: String,
    val subtitle: String?,
    val language: String?,
    val publicationDate: String?,
    val description: String?,
    val rating: Float?,
    val suffix: String?,
    val statusUuid: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

/**
 * SQLDelight entity for persons table.
 */
data class PersonSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val fileAs: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : PersonEntity

/**
 * SQLDelight entity for series table.
 */
data class SeriesSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val featured: Int?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : SeriesEntity

/**
 * SQLDelight entity for book-series relationship.
 */
data class BookSeriesSqlDelightEntity(
    override val bookUuid: String,
    override val seriesUuid: String,
    override val position: Int?,
) : BookSeriesEntity

/**
 * SQLDelight entity for tags table.
 */
data class TagSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : TagEntity

/**
 * SQLDelight entity for collections table.
 */
data class CollectionSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : CollectionEntity

/**
 * SQLDelight entity for statuses table.
 */
data class StatusSqlDelightEntity(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : StatusEntity

/**
 * SQLDelight entity for media_files table.
 */
data class MediaFileSqlDelightEntity(
    override val uuid: String,
    override val bookUuid: String,
    override val type: String,
    override val filepath: String,
    override val missing: Int?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : MediaFileEntity

/**
 * SQLDelight entity for readalouds table.
 */
data class ReadaloudSqlDelightEntity(
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

/**
 * SQLDelight entity for reading_progress table.
 */
data class ReadingProgressSqlDelightEntity(
    override val bookUuid: String,
    override val locatorHref: String?,
    override val locatorType: String?,
    override val locatorTitle: String?,
    override val progression: Double?,
    override val totalProgression: Double?,
    override val chapterIndex: Int?,
    override val totalChapters: Int?,
    override val audioTimestampMs: Long?,
    override val lastReadAt: String,
) : ReadingProgressEntity
