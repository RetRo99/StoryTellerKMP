package com.retro99.preferences.implementation.usecase

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.observeObject
import com.retro99.user.api.UserRegistry
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for observing a user-scoped preference value reactively.
 * Automatically scopes the preference to the current active user.
 */
@Factory
class ObserveUserPreferenceUseCase(
    @Provided @PublishedApi internal val preferences: Preferences,
    @Provided @PublishedApi internal val userRegistry: UserRegistry,
) {
    inline operator fun <reified T> invoke(key: PreferencesKey): Flow<T?> {
        val userId = userRegistry.getActiveProfileIdOrDefault()
        val userScopedKey = PreferencesKey.UserScoped(userId, key.name)
        return preferences.observeObject<T>(userScopedKey)
    }
}
