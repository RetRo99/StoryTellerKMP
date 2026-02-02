package com.retro99.books.data.source

import com.retro99.base.nowMillis
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.BookApiModel
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.BooksDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

private const val CACHE_VALIDITY_DURATION_MS = 30 * 60 * 1000L // 30 minutes

@Single(binds = [BooksLocalSource::class])
internal class BooksRoomDataSource(
    @Provided private val booksDatabase: BooksDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
    @Provided private val json: Json,
) : BooksLocalSource {

    override fun getBooks(): Flow<List<BookApiModel>> {
        return booksDatabase.getAllBooks().map { entities ->
            entities.mapNotNull { entity -> entity.decodeToApiModel() }
        }
    }

    override suspend fun getBook(uuid: String): AppResult<BookApiModel> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.getBookByUuid(uuid)?.decodeToApiModel()
                ?: throw NoSuchElementException("Book not found in cache")
        }
    }

    override suspend fun saveBooks(books: List<BookApiModel>): AppResult<Unit> {
        return databaseExecutor.executeDatabaseOperation {
            val currentTime = nowMillis()
            val entities = books.map { it.toEntity(currentTime) }
            booksDatabase.upsertBooks(entities)
        }
    }

    override suspend fun saveBook(book: BookApiModel): AppResult<Unit> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.upsertBook(book.toEntity(nowMillis()))
        }
    }

    override suspend fun clearCache(): AppResult<Unit> {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.deleteAllBooks()
        }
    }

    override suspend fun isCacheValid(): AppResult<Boolean> {
        return databaseExecutor.executeDatabaseOperation {
            val count = booksDatabase.getBooksCount()
            if (count == 0) return@executeDatabaseOperation false

            val oldBooks = booksDatabase.getBooksOlderThan(nowMillis() - CACHE_VALIDITY_DURATION_MS)
            oldBooks.isEmpty()
        }
    }

    private fun BookEntity.decodeToApiModel(): BookApiModel? {
        return runCatching {
            json.decodeFromString<BookApiModel>(dataJson)
        }.getOrNull()
    }

    private fun BookApiModel.toEntity(cachedAt: Long): BookEntity {
        return BooksLocalModel(
            uuid = uuid,
            title = title,
            id = id,
            rating = rating,
            dataJson = json.encodeToString(this),
            cachedAt = cachedAt,
        )
    }
}

private data class BooksLocalModel(
    override val uuid: String,
    override val title: String,
    override val id: Long,
    override val rating: Float?,
    override val dataJson: String,
    override val cachedAt: Long,
) : BookEntity

