package com.retro99.reader.domain.usecase

import com.retro99.base.result.CompletableResult
import com.retro99.books.domain.model.PositionDomainModel
import com.retro99.reader.domain.ReaderRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class SaveReadingProgressUseCase(
    @Provided private val readerRepository: ReaderRepository,
) {
    suspend operator fun invoke(progress: PositionDomainModel): CompletableResult {
        return readerRepository.saveReadingProgress(progress)
    }
}

