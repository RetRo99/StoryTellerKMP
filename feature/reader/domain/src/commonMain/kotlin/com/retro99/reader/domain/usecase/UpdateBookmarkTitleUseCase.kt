package com.retro99.reader.domain.usecase

import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.ReaderSettingsRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class UpdateBookmarkTitleUseCase(
    @Provided private val repository: ReaderSettingsRepository,
) {
    suspend operator fun invoke(id: String, title: String): CompletableResult {
        return repository.updateBookmarkTitle(id, title)
    }
}
