package com.retro99.books.domain.usecase

import com.retro99.books.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class ObserveFavoriteUseCase(
    @Provided private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(bookUuid: String): Flow<Boolean> {
        return favoritesRepository.observeIsFavorite(bookUuid)
    }
}

