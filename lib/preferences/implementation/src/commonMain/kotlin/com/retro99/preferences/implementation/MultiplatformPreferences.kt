package com.retro99.preferences.implementation

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.russhwolf.settings.Settings
import org.koin.core.annotation.Single

@Single(binds = [Preferences::class])
class MultiplatformPreferences(
    private val settings: Settings,
) : Preferences {

    override fun getStringOrNull(key: PreferencesKey): String? = settings.getStringOrNull(key.name)

    override fun putString(key: PreferencesKey, value: String) =
        settings.putString(key.name, value)

    override fun getBoolean(key: PreferencesKey, defaultValue: Boolean): Boolean =
        settings.getBoolean(key.name, defaultValue)

    override fun putBoolean(key: PreferencesKey, value: Boolean) =
        settings.putBoolean(key.name, value)

    override fun getLong(key: PreferencesKey, defaultValue: Long): Long =
        settings.getLong(key.name, defaultValue)

    override fun putLong(key: PreferencesKey, value: Long) =
        settings.putLong(key.name, value)

    override fun remove(key: PreferencesKey) {
        settings.remove(key.name)
    }
}