package com.retro99.preferences.implementation.di

import android.content.Context
import com.retro99.preferences.implementation.EncryptedPreferenceFactory
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android implementation of platform-specific Preferences module.
 * Registers EncryptedPreferenceFactory as the Settings.Factory implementation.
 */
actual val platformPreferencesModule: Module = module {
    single<Settings.Factory> { EncryptedPreferenceFactory(get<Context>()) }
}

