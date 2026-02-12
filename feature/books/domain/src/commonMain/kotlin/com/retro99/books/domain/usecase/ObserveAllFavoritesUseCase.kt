package com.retro99.books.domain.usecase

import com.retro99.books.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class ObserveAllFavoritesUseCase(
    @Provided private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(): Flow<Set<String>> {
        return favoritesRepository.observeAllFavoriteUuids()
    }
}

