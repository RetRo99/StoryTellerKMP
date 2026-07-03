package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderSettingsRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for clearing the currently reading book.
 */
@Factory
class ClearCurrentlyReadingUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    operator fun invoke() {
        readerSettingsRepository.clearCurrentlyReading()
    }
}
