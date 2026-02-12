package com.retro99.books.domain

import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.PersonDomainModel
import kotlinx.coroutines.flow.Flow

interface AuthorsRepository {

    fun getAuthors(): Flow<AppResult<List<PersonDomainModel>>>
}

