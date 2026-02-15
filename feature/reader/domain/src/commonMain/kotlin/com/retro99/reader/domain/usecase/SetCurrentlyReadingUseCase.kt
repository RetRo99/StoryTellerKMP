package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.CurrentlyReadingDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for setting the currently reading book.
 * Should only be called when a reading session meets the minimum duration requirement.
 */
@Factory
class SetCurrentlyReadingUseCase(
    @Provided private val readerRepository: ReaderRepository,
) {
    operator fun invoke(currentlyReading: CurrentlyReadingDomainModel) {
        readerRepository.setCurrentlyReading(currentlyReading)
    }
}

