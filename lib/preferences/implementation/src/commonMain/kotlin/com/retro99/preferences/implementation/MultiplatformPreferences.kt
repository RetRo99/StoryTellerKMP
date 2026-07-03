package com.retro99.preferences.implementation

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [Preferences::class])
class MultiplatformPreferences(
    settings: Settings,
) : Preferences {

    private val observableSettings = settings as ObservableSettings
    private val flowSettings = observableSettings.toFlowSettings()

    override fun getStringOrNull(key: PreferencesKey): String? = observableSettings.getStringOrNull(key.name)

    override fun putString(key: PreferencesKey, value: String) =
        observableSettings.putString(key.name, value)

    override fun observeStringOrNull(key: PreferencesKey): Flow<String?> =
        flowSettings.getStringOrNullFlow(key.name)

    override fun getBoolean(key: PreferencesKey, defaultValue: Boolean): Boolean =
        observableSettings.getBoolean(key.name, defaultValue)

    override fun putBoolean(key: PreferencesKey, value: Boolean) =
        observableSettings.putBoolean(key.name, value)

    override fun observeBoolean(key: PreferencesKey, defaultValue: Boolean): Flow<Boolean> =
        flowSettings.getBooleanFlow(key.name, defaultValue)

    override fun getLong(key: PreferencesKey, defaultValue: Long): Long =
        observableSettings.getLong(key.name, defaultValue)

    override fun putLong(key: PreferencesKey, value: Long) =
        observableSettings.putLong(key.name, value)

    override fun remove(key: PreferencesKey) {
        observableSettings.remove(key.name)
    }
}