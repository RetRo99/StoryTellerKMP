package com.retro99.preferences.implementation.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformPreferencesModule {

    @Single
    fun providesSettingsFactory(): Settings.Factory = object : Settings.Factory {
        override fun create(name: String?): Settings = StorageSettings()
    }
}
