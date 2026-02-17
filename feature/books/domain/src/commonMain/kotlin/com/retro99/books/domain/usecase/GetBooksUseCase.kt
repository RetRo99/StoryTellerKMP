package com.retro99.books.domain.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppResult
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetBooksUseCase(
    @Provided private val repositories: List<BooksRepository>,
) {
    operator fun invoke(): Flow<AppResult<List<BookDomainModel>>> {
        return combine(repositories.map { it.getBooks() }) { results ->
            val allBooks = results.flatMap { it.getOrElse { emptyList() } }
            Ok(allBooks)
        }
    }
}

