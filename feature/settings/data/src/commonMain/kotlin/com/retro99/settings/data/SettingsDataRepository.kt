package com.retro99.settings.data

import com.retro99.auth.domain.AuthRepository
import com.retro99.settings.domain.SettingsRepository
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [SettingsRepository::class])
internal class SettingsDataRepository(
    @Provided private val authRepository: AuthRepository,
) : SettingsRepository {

    override suspend fun logout() {
        authRepository.clearCredentials()
    }
}

