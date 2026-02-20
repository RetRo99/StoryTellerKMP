package com.retro99.preferences.implementation.usecase

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.putObject
import com.retro99.user.api.UserRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for saving a user-scoped preference value.
 * Automatically scopes the preference to the current active user.
 */
@Factory
class SaveUserPreferenceUseCase(
    @Provided @PublishedApi internal val preferences: Preferences,
    @Provided @PublishedApi internal val userRegistry: UserRegistry,
) {
    /**
     * Saves a user-scoped preference value.
     *
     * @param key The base preference key (will be scoped to current user)
     * @param value The value to save
     */
    inline operator fun <reified T> invoke(key: PreferencesKey, value: T) {
        val userId = userRegistry.getActiveProfileIdOrDefault()
        val userScopedKey = PreferencesKey.UserScoped(userId, key.name)
        preferences.putObject(userScopedKey, value)
    }
}

