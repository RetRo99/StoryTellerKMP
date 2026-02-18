package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetReaderSettingsUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    operator fun invoke(): Flow<ReaderSettingsDomainModel> {
        return readerSettingsRepository.getReaderSettings()
    }
}

