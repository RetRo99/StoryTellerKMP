package com.retro99.settings.domain.usecase

import com.retro99.settings.domain.SettingsRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class LogoutUseCase(
    @Provided private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke() {
        settingsRepository.logout()
    }
}

