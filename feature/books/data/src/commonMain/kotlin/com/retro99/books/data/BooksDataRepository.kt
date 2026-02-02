package com.retro99.books.data

import com.github.michaelbull.result.map
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.toDomain
import com.retro99.books.data.source.BooksLocalSource
import com.retro99.books.data.source.BooksRemoteSource
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.BaseUrlProvider

@Single(binds = [BooksRepository::class])
internal class BooksDataRepository(
    @Provided private val remoteSource: BooksRemoteSource,
    @Provided private val localSource: BooksLocalSource,
    @Provided private val baseUrlProvider: BaseUrlProvider,
) : BooksRepository, BaseRepository {

    override fun getBooks(): Flow<AppResult<List<BookDomainModel>>> {
        val baseUrl = baseUrlProvider.getBaseUrl()
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getBooks()
            },
            remoteSource = {
                remoteSource.getBooks()
            },
            saveToCache = { books ->
                localSource.saveBooks(books)
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
                localSource.getBook(uuid)
            },
            remoteSource = {
                remoteSource.getBook(uuid)
            },
            saveToCache = { book ->
                localSource.saveBook(book)
            },
        ).map { result ->
            result.map { book ->
                book.toDomain(baseUrl)
            }
        }
    }
}
