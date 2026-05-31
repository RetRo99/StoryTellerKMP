package com.retro99.analytics.implementation

import com.retro99.analytics.api.FileLogger
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import kotlin.system.exitProcess

actual fun installCrashFileLogging(fileLogger: FileLogger, preferences: Preferences) {
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        if (preferences.getBoolean(PreferencesKey.FileLoggingEnabled, defaultValue = false)) {
            fileLogger.logException(
                throwable = throwable,
                message = "Fatal crash with full stack trace on thread: ${thread.name}",
            )
        }

        if (previousHandler != null) {
            previousHandler.uncaughtException(thread, throwable)
        } else {
            exitProcess(2)
        }
    }
}
