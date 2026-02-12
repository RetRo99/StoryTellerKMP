package com.retro99.database.api.favorites

import kotlinx.coroutines.flow.Flow

/**
 * Database interface for favorites-related operations.
 */
interface FavoritesDatabase {

    suspend fun addFavorite(bookUuid: String)

    suspend fun removeFavorite(bookUuid: String)

    suspend fun isFavorite(bookUuid: String): Boolean

    fun observeFavorite(bookUuid: String): Flow<Boolean>

    suspend fun getAllFavoriteBookUuids(): List<String>

    suspend fun deleteAllFavorites()
}

