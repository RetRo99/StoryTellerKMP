package com.retro99.analytics.implementation

import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.AnalyticsEvent
import com.retro99.analytics.api.FileLogger
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey

class DebugAnalyticsManager(
    private val fileLogger: FileLogger,
    private val preferences: Preferences,
) : Analytics {

    private val logger = Logger.withTag("DebugAnalyticsManager")

    override fun logException(throwable: Throwable, message: String?) {
        logger.e(throwable) {
            if (message.isNullOrEmpty()) {
                "Exception occurred"
            } else {
                "Exception occurred with message: $message"
            }
        }
        // Also log to file for user sharing (if enabled)
        if (isFileLoggingEnabled()) {
            fileLogger.logException(throwable, message)
        }
    }

    override fun logEvent(event: AnalyticsEvent) {
        val eventMessage = buildString {
            append("Analytics Event: ${event.name}")
            if (event.parameters.isNotEmpty()) {
                append(" | Parameters: ${event.parameters}")
            }
        }
        logger.d { eventMessage }
    }

    override fun setUserId(userId: String?) {
        logger.d { "Set User ID: ${userId ?: "null (cleared)"}" }
    }

    private fun isFileLoggingEnabled(): Boolean {
        return preferences.getBoolean(PreferencesKey.FileLoggingEnabled, defaultValue = false)
    }
}