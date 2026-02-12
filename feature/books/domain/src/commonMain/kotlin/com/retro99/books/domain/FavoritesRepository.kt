package com.retro99.books.domain

import com.retro99.base.result.CompletableResult
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    suspend fun addToFavorites(bookUuid: String): CompletableResult

    suspend fun removeFromFavorites(bookUuid: String): CompletableResult

    fun observeIsFavorite(bookUuid: String): Flow<Boolean>

    suspend fun isFavorite(bookUuid: String): Boolean

    fun observeAllFavoriteUuids(): Flow<Set<String>>
}

