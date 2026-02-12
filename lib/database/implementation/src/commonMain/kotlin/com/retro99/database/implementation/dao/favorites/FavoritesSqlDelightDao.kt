package com.retro99.database.implementation.dao.favorites

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.retro99.base.nowMillis
import com.retro99.database.implementation.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * SQLDelight DAO for favorites table operations.
 */
internal class FavoritesSqlDelightDao(
    private val database: AppDatabase,
) {
    private val favoriteQueries = database.favoriteQueries

    suspend fun insertFavorite(bookUuid: String) {
        withContext(Dispatchers.IO) {

            favoriteQueries.insertFavorite(bookUuid, nowMillis().toString())
        }
    }

    suspend fun deleteFavorite(bookUuid: String) {
        withContext(Dispatchers.IO) {
            favoriteQueries.deleteFavorite(bookUuid)
        }
    }

    suspend fun isFavorite(bookUuid: String): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteQueries.isFavorite(bookUuid).executeAsOne() > 0
        }
    }

    fun observeFavorite(bookUuid: String): Flow<Boolean> {
        return favoriteQueries.isFavorite(bookUuid)
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { count -> count > 0 }
    }

    suspend fun getAllFavorites(): List<String> {
        return withContext(Dispatchers.IO) {
            favoriteQueries.getAllFavorites().executeAsList()
        }
    }

    fun observeAllFavorites(): Flow<List<String>> {
        return favoriteQueries.getAllFavorites()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun deleteAllFavorites() {
        withContext(Dispatchers.IO) {
            favoriteQueries.deleteAllFavorites()
        }
    }
}

