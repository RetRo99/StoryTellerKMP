package com.retro99.analytics.implementation

import com.retro99.analytics.api.FileLogger
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSSetUncaughtExceptionHandler

private var crashFileLogger: FileLogger? = null
private var crashPreferences: Preferences? = null

@OptIn(ExperimentalForeignApi::class)
actual fun installCrashFileLogging(fileLogger: FileLogger, preferences: Preferences) {
    crashFileLogger = fileLogger
    crashPreferences = preferences

    NSSetUncaughtExceptionHandler(staticCFunction { exception ->
        val logger = crashFileLogger
        val prefs = crashPreferences
        if (logger == null || prefs == null) {
            return@staticCFunction
        }
        if (!prefs.getBoolean(PreferencesKey.FileLoggingEnabled, defaultValue = false)) {
            return@staticCFunction
        }

        val name = exception?.name ?: "UnknownException"
        val reason = exception?.reason ?: "No reason provided"
        val stack = exception?.callStackSymbols?.joinToString("\n") ?: "No stack trace"

        logger.logException(
            throwable = RuntimeException("$name: $reason"),
            message = "Fatal NSException: $name\nReason: $reason\nStack:\n$stack",
        )
    })
}
