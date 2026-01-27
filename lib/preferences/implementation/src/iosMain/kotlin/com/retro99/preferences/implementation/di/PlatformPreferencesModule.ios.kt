package com.retro99.preferences.implementation.di

import com.retro99.preferences.implementation.IosSettingsFactory
import com.russhwolf.settings.Settings
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific Preferences module.
 * Registers IosSettingsFactory as the Settings.Factory implementation.
 */
@Module
actual class PlatformPreferencesModule {

    @Single
    fun providesSettingsFactory(): Settings.Factory {
        return IosSettingsFactory()
    }
}

