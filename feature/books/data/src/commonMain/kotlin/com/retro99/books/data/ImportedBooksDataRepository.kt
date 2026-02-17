package com.retro99.books.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.books.data.source.ImportedBooksLocalSource
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [BooksRepository::class])
internal class ImportedBooksDataRepository(
    @Provided private val localSource: ImportedBooksLocalSource,
) : BooksRepository {

    override fun getBooks(): Flow<AppResult<List<BookDomainModel>>> {
        return localSource.observeAllImportedBooks().map { books ->
            Ok(books)
        }
    }

    override fun getBook(uuid: String): Flow<AppResult<BookDomainModel>> {
        return localSource.observeAllImportedBooks().map { books ->
            val book = books.find { it.uuid == uuid }
            if (book != null) {
                Ok(book)
            } else {
                Err(AppError.UnknownError(Throwable("Imported book not found: $uuid")))
            }
        }
    }
}

