package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.BookmarkDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class ObserveBookmarksUseCase(
    @Provided private val repository: ReaderSettingsRepository,
) {
    operator fun invoke(bookUuid: String): Flow<List<BookmarkDomainModel>> {
        return repository.observeBookmarks(bookUuid)
    }
}
