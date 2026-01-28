package com.retro99.books.data

import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.toDomain
import com.retro99.books.data.source.BooksRemoteSource
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [BooksRepository::class])
internal class BooksDataRepository(
    @Provided private val remoteSource: BooksRemoteSource,
) : BooksRepository {

    override suspend fun getBooks(): AppResult<List<BookDomainModel>> {
        return remoteSource.getBooks().map { books ->
            books.map { it.toDomain() }
        }
    }

    override suspend fun getBook(uuid: String): AppResult<BookDomainModel> {
        return remoteSource.getBook(uuid).map { it.toDomain() }
    }
}

