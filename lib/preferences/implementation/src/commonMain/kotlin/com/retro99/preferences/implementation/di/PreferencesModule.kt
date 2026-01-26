package com.retro99.preferences.implementation.di

import com.russhwolf.settings.Settings
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan("com.retro99.preferences.implementation")
class PreferencesModule {

    @Single
    fun provideSettings(@Provided factory: Settings.Factory): Settings {
        return factory.create("SecureSettings")
    }
}

