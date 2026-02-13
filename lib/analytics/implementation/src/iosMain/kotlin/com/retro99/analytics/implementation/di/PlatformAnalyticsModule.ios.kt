package com.retro99.analytics.implementation.di

import com.retro99.analytics.api.FileLogger
import com.retro99.analytics.implementation.IosFileLogger
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific Analytics module.
 * Registers IosFileLogger as the FileLogger implementation.
 */
@Module
actual class PlatformAnalyticsModule {

    @Single
    fun provideFileLogger(): FileLogger {
        return IosFileLogger()
    }
}

