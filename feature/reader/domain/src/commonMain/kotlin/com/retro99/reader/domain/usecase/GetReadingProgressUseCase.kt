package com.retro99.reader.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.ReadingProgressDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetReadingProgressUseCase(
    @Provided private val readerRepository: ReaderRepository,
) {
    suspend operator fun invoke(bookUuid: String): AppResult<ReadingProgressDomainModel?> {
        return readerRepository.getReadingProgress(bookUuid)
    }
}

