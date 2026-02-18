package com.retro99.login.domain.usecase

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class SkipLoginUseCase(
    @Provided private val preferences: Preferences,
) {
    operator fun invoke() {
        preferences.putBoolean(PreferencesKey.SkippedLogin, true)
    }
}

