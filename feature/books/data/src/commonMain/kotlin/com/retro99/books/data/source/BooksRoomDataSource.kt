package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.BookLocalModel
import com.retro99.books.data.model.CollectionLocalModel
import com.retro99.books.data.model.MediaFileLocalModel
import com.retro99.books.data.model.PersonLocalModel
import com.retro99.books.data.model.PositionLocalModel
import com.retro99.books.data.model.ReadaloudLocalModel
import com.retro99.books.data.model.SeriesWithPositionLocalModel
import com.retro99.books.data.model.StatusLocalModel
import com.retro99.books.data.model.TagLocalModel
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.BooksDatabase
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [BooksLocalSource::class])
internal class BooksRoomDataSource(
    @Provided private val booksDatabase: BooksDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : BooksLocalSource {

    override suspend fun getBooks(): AppResult<List<BookLocalModel>?> {
        return databaseExecutor.executeDatabaseOperation {
            val books = booksDatabase.getAllBooks()
            if (books.isEmpty()) {
                null
            } else {
                books.map { it.toLocalModel() }
            }
        }
    }

    override suspend fun getBook(uuid: String): AppResult<BookLocalModel?> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.getBookByUuid(uuid)?.toLocalModel()
        }
    }

    override suspend fun saveBooks(books: List<BookLocalModel>): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            books.forEach { book ->
                booksDatabase.upsertBook(book)
            }
        }
    }

    override suspend fun saveBook(book: BookLocalModel): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.upsertBook(book)
        }
    }

    override suspend fun clearCache(): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.deleteAllRelatedData()
        }
    }

    private fun BookEntity.toLocalModel(): BookLocalModel {
        return BookLocalModel(
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
            authors = authors.map {
                PersonLocalModel(
                    uuid = it.uuid,
                    name = it.name,
                    fileAs = it.fileAs,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            narrators = narrators.map {
                PersonLocalModel(
                    uuid = it.uuid,
                    name = it.name,
                    fileAs = it.fileAs,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            creators = creators.map {
                PersonLocalModel(
                    uuid = it.uuid,
                    name = it.name,
                    fileAs = it.fileAs,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            series = series.map {
                SeriesWithPositionLocalModel(
                    uuid = it.uuid,
                    name = it.name,
                    featured = it.featured,
                    position = it.position,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            tags = tags.map {
                TagLocalModel(
                    uuid = it.uuid,
                    name = it.name,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            collections = collections.map {
                CollectionLocalModel(
                    uuid = it.uuid,
                    name = it.name,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            status = status?.let {
                StatusLocalModel(
                    uuid = it.uuid,
                    name = it.name,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            position = position?.let {
                PositionLocalModel(
                    bookUuid = it.bookUuid,
                    uuid = it.uuid,
                    timestamp = it.timestamp,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    locatorHref = it.locatorHref,
                    locatorType = it.locatorType,
                    locatorTitle = it.locatorTitle,
                    locatorTarget = it.locatorTarget,
                    audioTimestampMs = it.audioTimestampMs,
                    chapterIndex = it.chapterIndex,
                    progression = it.progression,
                    totalChapters = it.totalChapters,
                    totalDurationMs = it.totalDurationMs,
                    totalProgression = it.totalProgression,
                    position = it.position,
                )
            },
            ebook = ebook?.let {
                MediaFileLocalModel(
                    uuid = it.uuid,
                    bookUuid = it.bookUuid,
                    type = it.type,
                    filepath = it.filepath,
                    missing = it.missing,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            audiobook = audiobook?.let {
                MediaFileLocalModel(
                    uuid = it.uuid,
                    bookUuid = it.bookUuid,
                    type = it.type,
                    filepath = it.filepath,
                    missing = it.missing,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            readaloud = readaloud?.let {
                ReadaloudLocalModel(
                    uuid = it.uuid,
                    bookUuid = it.bookUuid,
                    filepath = it.filepath,
                    missing = it.missing,
                    status = it.status,
                    currentStage = it.currentStage,
                    stageProgress = it.stageProgress,
                    queuePosition = it.queuePosition,
                    restartPending = it.restartPending,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
        )
    }
}
