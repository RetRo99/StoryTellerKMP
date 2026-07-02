package com.retro99.analytics.implementation.di

import com.retro99.analytics.api.FileLogger
import com.retro99.analytics.implementation.WebFileLogger
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformAnalyticsModule {

    @Single
    fun provideFileLogger(): FileLogger = WebFileLogger()
}
