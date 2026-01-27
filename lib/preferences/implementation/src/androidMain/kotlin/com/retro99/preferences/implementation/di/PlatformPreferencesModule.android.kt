package com.retro99.preferences.implementation.di

import android.content.Context
import com.retro99.preferences.implementation.EncryptedPreferenceFactory
import com.russhwolf.settings.Settings
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific Preferences module.
 * Registers EncryptedPreferenceFactory as the Settings.Factory implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformPreferencesModule {

    @Single
    fun providesSettingsFactory(context: Context): Settings.Factory {
        return EncryptedPreferenceFactory(context)
    }
}

