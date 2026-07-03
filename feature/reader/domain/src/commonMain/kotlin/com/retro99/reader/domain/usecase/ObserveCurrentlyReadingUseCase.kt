package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.CurrentlyReadingDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for observing the currently reading book reactively.
 */
@Factory
class ObserveCurrentlyReadingUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    operator fun invoke(): Flow<CurrentlyReadingDomainModel?> {
        return readerSettingsRepository.observeCurrentlyReading()
    }
}
