package com.retro99.reader.domain.usecase

import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.BookmarkDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class AddBookmarkUseCase(
    @Provided private val repository: ReaderSettingsRepository,
) {
    suspend operator fun invoke(bookmark: BookmarkDomainModel): CompletableResult {
        return repository.addBookmark(bookmark)
    }
}
