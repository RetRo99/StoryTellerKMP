package com.retro99.books.domain.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetBookByUuidUseCase(
    @Provided private val repositories: List<BooksRepository>,
) {
    operator fun invoke(uuid: String): Flow<AppResult<BookDomainModel>> {
        return combine(repositories.map { it.getBook(uuid) }) { results ->
            results.mapNotNull { it.getOrElse { null } }.firstOrNull()
        }.map { book ->
            book?.let { com.github.michaelbull.result.Ok(it) }
                ?: Err(AppError.UnknownError(Throwable("Book not found: $uuid")))
        }
    }
}

