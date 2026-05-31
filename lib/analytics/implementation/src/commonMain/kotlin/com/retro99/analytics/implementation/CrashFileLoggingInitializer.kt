package com.retro99.analytics.implementation

import com.retro99.analytics.api.FileLogger
import com.retro99.base.AppInitializer
import com.retro99.preferences.api.Preferences
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [AppInitializer::class])
class CrashFileLoggingInitializer(
    @Provided private val fileLogger: FileLogger,
    @Provided private val preferences: Preferences,
) : AppInitializer {
    override fun initialize() {
        installCrashFileLogging(fileLogger, preferences)
    }
}

expect fun installCrashFileLogging(fileLogger: FileLogger, preferences: Preferences)
