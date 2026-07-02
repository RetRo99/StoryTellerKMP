package com.retro99.analytics.implementation.di

import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.FileLogger
import com.retro99.analytics.implementation.DebugAnalyticsManager
import com.retro99.preferences.api.Preferences
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(
    includes = [
        PlatformAnalyticsModule::class,
    ],
)
@Configuration
@ComponentScan("com.retro99.analytics.implementation")
class AnalyticsModule {

    @Single
    fun provideAnalytics(
        fileLogger: FileLogger,
        @Provided preferences: Preferences,
        @Named("isDebug") isDebug: Boolean,
    ): Analytics = if (isDebug) {
        DebugAnalyticsManager(fileLogger, preferences)
    } else {
        createProductionAnalytics(fileLogger, preferences)
    }
}

internal expect fun createProductionAnalytics(
    fileLogger: FileLogger,
    preferences: Preferences,
): Analytics
