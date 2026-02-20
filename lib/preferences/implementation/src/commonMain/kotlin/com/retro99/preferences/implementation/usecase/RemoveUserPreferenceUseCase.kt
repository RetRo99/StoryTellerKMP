package com.retro99.preferences.implementation.usecase

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.user.api.UserRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for removing a user-scoped preference value.
 * Automatically scopes the preference to the current active user.
 */
@Factory
class RemoveUserPreferenceUseCase(
    @Provided private val preferences: Preferences,
    @Provided private val userRegistry: UserRegistry,
) {
    /**
     * Removes a user-scoped preference value.
     *
     * @param key The base preference key (will be scoped to current user)
     */
    operator fun invoke(key: PreferencesKey) {
        val userId = userRegistry.getActiveProfileIdOrDefault()
        val userScopedKey = PreferencesKey.UserScoped(userId, key.name)
        preferences.remove(userScopedKey)
    }
}

