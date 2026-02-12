package com.retro99.database.implementation.dao.favorites

import com.retro99.database.api.favorites.FavoritesDatabase
import kotlinx.coroutines.flow.Flow

internal class FavoritesDatabaseImpl(
    private val sqlDelightDao: FavoritesSqlDelightDao,
) : FavoritesDatabase {

    override suspend fun addFavorite(bookUuid: String) {
        sqlDelightDao.insertFavorite(bookUuid)
    }

    override suspend fun removeFavorite(bookUuid: String) {
        sqlDelightDao.deleteFavorite(bookUuid)
    }

    override suspend fun isFavorite(bookUuid: String): Boolean {
        return sqlDelightDao.isFavorite(bookUuid)
    }

    override fun observeFavorite(bookUuid: String): Flow<Boolean> {
        return sqlDelightDao.observeFavorite(bookUuid)
    }

    override suspend fun getAllFavoriteBookUuids(): List<String> {
        return sqlDelightDao.getAllFavorites()
    }

    override suspend fun deleteAllFavorites() {
        sqlDelightDao.deleteAllFavorites()
    }
}

