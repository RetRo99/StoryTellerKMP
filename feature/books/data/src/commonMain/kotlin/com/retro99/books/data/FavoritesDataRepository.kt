package com.retro99.books.data

import com.retro99.base.result.CompletableResult
import com.retro99.books.data.source.FavoritesLocalSource
import com.retro99.books.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [FavoritesRepository::class])
internal class FavoritesDataRepository(
    @Provided private val localSource: FavoritesLocalSource,
) : FavoritesRepository {

    override suspend fun addToFavorites(bookUuid: String): CompletableResult {
        return localSource.addFavorite(bookUuid)
    }

    override suspend fun removeFromFavorites(bookUuid: String): CompletableResult {
        return localSource.removeFavorite(bookUuid)
    }

    override fun observeIsFavorite(bookUuid: String): Flow<Boolean> {
        return localSource.observeFavorite(bookUuid)
    }

    override suspend fun isFavorite(bookUuid: String): Boolean {
        return localSource.isFavorite(bookUuid)
    }
}

