package com.retro99.preferences.implementation.di

import com.retro99.preferences.implementation.IosSettingsFactory
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS implementation of platform-specific Preferences module.
 * Registers IosSettingsFactory as the Settings.Factory implementation.
 */
actual val platformPreferencesModule: Module = module {
    single<Settings.Factory> { IosSettingsFactory() }
}

