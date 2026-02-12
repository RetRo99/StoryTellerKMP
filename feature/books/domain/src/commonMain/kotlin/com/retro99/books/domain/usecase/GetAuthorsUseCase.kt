package com.retro99.books.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.books.domain.AuthorsRepository
import com.retro99.books.domain.model.PersonDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetAuthorsUseCase(
    @Provided private val authorsRepository: AuthorsRepository,
) {
    operator fun invoke(): Flow<AppResult<List<PersonDomainModel>>> {
        return authorsRepository.getAuthors()
    }
}

