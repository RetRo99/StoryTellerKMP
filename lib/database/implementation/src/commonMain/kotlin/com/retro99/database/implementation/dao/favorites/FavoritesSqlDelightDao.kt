package com.retro99.database.implementation.dao.favorites

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.retro99.base.nowMillis
import com.retro99.database.implementation.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * SQLDelight DAO for favorites table operations.
 */
internal class FavoritesSqlDelightDao(
    private val databaseManager: DatabaseManager,
) {
    private val favoriteQueries get() = databaseManager.getDatabase().favoriteQueries

    suspend fun insertFavorite(bookUuid: String) {
        withContext(Dispatchers.Default) {

            favoriteQueries.insertFavorite(bookUuid, nowMillis().toString())
        }
    }

    suspend fun deleteFavorite(bookUuid: String) {
        withContext(Dispatchers.Default) {
            favoriteQueries.deleteFavorite(bookUuid)
        }
    }

    suspend fun isFavorite(bookUuid: String): Boolean {
        return withContext(Dispatchers.Default) {
            favoriteQueries.isFavorite(bookUuid).executeAsOne() > 0
        }
    }

    fun observeFavorite(bookUuid: String): Flow<Boolean> {
        return favoriteQueries.isFavorite(bookUuid)
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { count -> count > 0 }
    }

    suspend fun getAllFavorites(): List<String> {
        return withContext(Dispatchers.Default) {
            favoriteQueries.getAllFavorites().executeAsList()
        }
    }

    fun observeAllFavorites(): Flow<List<String>> {
        return favoriteQueries.getAllFavorites()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    suspend fun deleteAllFavorites() {
        withContext(Dispatchers.Default) {
            favoriteQueries.deleteAllFavorites()
        }
    }
}

