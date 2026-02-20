package com.retro99.preferences.implementation.usecase

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.user.api.UserRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting a user-scoped preference value.
 * Automatically scopes the preference to the current active user.
 */
@Factory
class GetUserPreferenceUseCase(
    @Provided @PublishedApi internal val preferences: Preferences,
    @Provided @PublishedApi internal val userRegistry: UserRegistry,
) {
    /**
     * Gets a user-scoped preference value.
     *
     * @param key The base preference key (will be scoped to current user)
     * @return The preference value, or null if not set
     */
    inline operator fun <reified T> invoke(key: PreferencesKey): T? {
        val userId = userRegistry.getActiveProfileIdOrDefault()
        val userScopedKey = PreferencesKey.UserScoped(userId, key.name)
        return preferences.getObject<T>(userScopedKey)
    }
}

