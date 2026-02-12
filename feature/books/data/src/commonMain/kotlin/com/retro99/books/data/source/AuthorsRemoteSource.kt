package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.data.model.PersonApiModel

interface AuthorsRemoteSource {

    suspend fun getAuthors(): AppResult<List<PersonApiModel>>
}

