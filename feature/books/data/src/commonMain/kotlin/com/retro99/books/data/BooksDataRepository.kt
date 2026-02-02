package com.retro99.books.data

import com.github.michaelbull.result.map
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.toDomain
import com.retro99.books.data.model.toLocal
import com.retro99.books.data.source.BooksLocalSource
import com.retro99.books.data.source.BooksRemoteSource
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import kotlinx.coroutines.flow.Flow
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
                localSource.getBooks().map { books ->
                    books?.map { it.toDomain(baseUrl) }
                }
            },
            remoteSource = {
                remoteSource.getBooks().map { books ->
                    books.map { it.toDomain(baseUrl) }
                }
            },
            saveToCache = { domainBooks ->
                localSource.saveBooks(domainBooks.map { it.toLocal() })
            },
        )
    }

    override fun getBook(uuid: String): Flow<AppResult<BookDomainModel>> {
        val baseUrl = baseUrlProvider.getBaseUrl()
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getBook(uuid).map { book ->
                    book?.toDomain(baseUrl)
                }
            },
            remoteSource = {
                remoteSource.getBook(uuid).map { book ->
                    book.toDomain(baseUrl)
                }
            },
            saveToCache = { domainBook ->
                localSource.saveBook(domainBook.toLocal())
            },
        )
    }
}
