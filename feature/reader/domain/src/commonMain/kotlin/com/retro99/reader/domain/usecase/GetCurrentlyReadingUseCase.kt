package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.CurrentlyReadingDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting the currently reading book.
 * Returns the last book that was read for at least the minimum required duration.
 */
@Factory
class GetCurrentlyReadingUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    operator fun invoke(): CurrentlyReadingDomainModel? {
        return readerSettingsRepository.getCurrentlyReading()
    }
}

