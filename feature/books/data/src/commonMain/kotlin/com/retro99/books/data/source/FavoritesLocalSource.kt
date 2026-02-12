package com.retro99.books.data.source

import com.retro99.base.result.CompletableResult
import kotlinx.coroutines.flow.Flow

interface FavoritesLocalSource {

    suspend fun addFavorite(bookUuid: String): CompletableResult

    suspend fun removeFavorite(bookUuid: String): CompletableResult

    suspend fun isFavorite(bookUuid: String): Boolean

    fun observeFavorite(bookUuid: String): Flow<Boolean>

    fun observeAllFavorites(): Flow<List<String>>
}

