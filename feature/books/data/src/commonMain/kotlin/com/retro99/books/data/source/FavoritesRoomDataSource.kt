package com.retro99.books.data.source

import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.favorites.FavoritesDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [FavoritesLocalSource::class])
internal class FavoritesRoomDataSource(
    @Provided private val favoritesDatabase: FavoritesDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : FavoritesLocalSource {

    override suspend fun addFavorite(bookUuid: String): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            favoritesDatabase.addFavorite(bookUuid)
        }
    }

    override suspend fun removeFavorite(bookUuid: String): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            favoritesDatabase.removeFavorite(bookUuid)
        }
    }

    override suspend fun isFavorite(bookUuid: String): Boolean {
        return favoritesDatabase.isFavorite(bookUuid)
    }

    override fun observeFavorite(bookUuid: String): Flow<Boolean> {
        return favoritesDatabase.observeFavorite(bookUuid)
    }
}

