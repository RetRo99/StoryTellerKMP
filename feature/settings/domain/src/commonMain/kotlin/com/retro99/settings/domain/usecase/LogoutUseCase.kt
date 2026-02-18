package com.retro99.settings.domain.usecase

import com.retro99.settings.domain.SettingsRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class LogoutUseCase(
    @Provided private val settingsRepository: SettingsRepository,
) {
    /**
     * Logout from a specific server or all servers.
     * @param serverId If provided, logout from that specific server. If null, logout from all servers.
     */
    suspend operator fun invoke(serverId: String? = null) {
        settingsRepository.logout(serverId)
    }
}

