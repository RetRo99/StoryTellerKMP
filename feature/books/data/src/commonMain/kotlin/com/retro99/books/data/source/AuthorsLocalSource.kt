package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.PersonLocalModel

interface AuthorsLocalSource {

    suspend fun getAuthors(): AppResult<List<PersonLocalModel>?>

    suspend fun saveAuthors(authors: List<PersonLocalModel>): CompletableResult

    suspend fun clearCache(): CompletableResult
}

