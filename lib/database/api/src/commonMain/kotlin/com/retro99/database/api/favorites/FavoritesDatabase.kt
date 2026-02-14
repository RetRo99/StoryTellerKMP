package com.retro99.database.api.favorites

import com.retro99.database.api.DataClearable
import kotlinx.coroutines.flow.Flow

/**
 * Database interface for favorites-related operations.
 */
interface FavoritesDatabase : DataClearable {

    suspend fun addFavorite(bookUuid: String)

    suspend fun removeFavorite(bookUuid: String)

    suspend fun isFavorite(bookUuid: String): Boolean

    fun observeFavorite(bookUuid: String): Flow<Boolean>

    suspend fun getAllFavoriteBookUuids(): List<String>

    fun observeAllFavorites(): Flow<List<String>>

    override suspend fun clearAllData()
}

