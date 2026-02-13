package com.retro99.analytics.implementation.di

import android.content.Context
import com.retro99.analytics.api.FileLogger
import com.retro99.analytics.implementation.AndroidFileLogger
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific Analytics module.
 * Registers AndroidFileLogger as the FileLogger implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformAnalyticsModule {

    @Single
    fun provideFileLogger(context: Context): FileLogger {
        return AndroidFileLogger(context)
    }
}

