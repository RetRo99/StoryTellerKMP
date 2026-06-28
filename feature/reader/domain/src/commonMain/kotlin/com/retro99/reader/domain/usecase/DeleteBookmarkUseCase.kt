package com.retro99.reader.domain.usecase

import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.ReaderSettingsRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class DeleteBookmarkUseCase(
    @Provided private val repository: ReaderSettingsRepository,
) {
    suspend operator fun invoke(id: String): CompletableResult {
        return repository.deleteBookmark(id)
    }
}
