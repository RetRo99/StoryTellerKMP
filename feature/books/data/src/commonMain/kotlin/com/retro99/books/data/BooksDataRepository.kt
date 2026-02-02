package com.retro99.books.data

import com.github.michaelbull.result.map
import com.retro99.base.nowMillis
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.BookApiModel
import com.retro99.books.data.model.toDomain
import com.retro99.books.data.source.BooksLocalSource
import com.retro99.books.data.source.BooksRemoteSource
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import com.retro99.database.api.books.BookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.BaseUrlProvider

@Single(binds = [BooksRepository::class])
internal class BooksDataRepository(
    @Provided private val remoteSource: BooksRemoteSource,
    @Provided private val localSource: BooksLocalSource,
    @Provided private val baseUrlProvider: BaseUrlProvider,
    @Provided private val json: Json,
) : BooksRepository, BaseRepository {

    override fun getBooks(): Flow<AppResult<List<BookDomainModel>>> {
        val baseUrl = baseUrlProvider.getBaseUrl()
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getBooks().map { entities ->
                    entities?.mapNotNull { it.toApiModel() }
                }
            },
            remoteSource = {
                remoteSource.getBooks()
            },
            saveToCache = { books ->
                localSource.saveBooks(books.map { it.toEntity() })
            },
        ).map { result ->
            result.map { books ->
                books.map { it.toDomain(baseUrl) }
            }
        }
    }

    override fun getBook(uuid: String): Flow<AppResult<BookDomainModel>> {
        val baseUrl = baseUrlProvider.getBaseUrl()
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getBook(uuid).map { it?.toApiModel() }
            },
            remoteSource = {
                remoteSource.getBook(uuid)
            },
            saveToCache = { book ->
                localSource.saveBook(book.toEntity())
            },
        ).map { result ->
            result.map { book ->
                book.toDomain(baseUrl)
            }
        }
    }

    private fun BookApiModel.toEntity(): BookEntity {
        return BookEntityImpl(
            uuid = uuid,
            title = title,
            id = id,
            rating = rating,
            dataJson = json.encodeToString(this),
            cachedAt = nowMillis(),
        )
    }

    private fun BookEntity.toApiModel(): BookApiModel? {
        return runCatching {
            json.decodeFromString<BookApiModel>(dataJson)
        }.getOrNull()
    }
}

private data class BookEntityImpl(
    override val uuid: String,
    override val title: String,
    override val id: Long,
    override val rating: Float?,
    override val dataJson: String,
    override val cachedAt: Long,
) : BookEntity

