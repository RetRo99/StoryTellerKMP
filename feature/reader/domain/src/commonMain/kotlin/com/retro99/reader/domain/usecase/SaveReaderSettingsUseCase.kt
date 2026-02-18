package com.retro99.reader.domain.usecase

import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class SaveReaderSettingsUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    suspend operator fun invoke(settings: ReaderSettingsDomainModel): CompletableResult {
        return readerSettingsRepository.saveReaderSettings(settings)
    }
}

