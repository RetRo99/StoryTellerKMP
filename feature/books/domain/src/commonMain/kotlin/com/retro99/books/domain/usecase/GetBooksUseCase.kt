package com.retro99.books.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.books.domain.BooksRepository
import com.retro99.books.domain.model.BookDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetBooksUseCase(
    @Provided private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(): AppResult<List<BookDomainModel>> {
        return booksRepository.getBooks()
    }
}

