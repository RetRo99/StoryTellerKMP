package com.retro99.preferences.implementation

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

@OptIn(ExperimentalSettingsImplementation::class)
class IosSettingsFactory : Settings.Factory {

    override fun create(name: String?): Settings {
        return KeychainSettings(service = name ?: "SecureSettings")
    }
}
