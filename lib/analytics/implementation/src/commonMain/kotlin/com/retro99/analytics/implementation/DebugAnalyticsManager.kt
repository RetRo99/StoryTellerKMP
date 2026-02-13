package com.retro99.analytics.implementation

import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.AnalyticsEvent
import com.retro99.analytics.api.FileLogger

class DebugAnalyticsManager(
    private val fileLogger: FileLogger,
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
        // Also log to file for user sharing
        fileLogger.logException(throwable, message)
    }

    override fun logEvent(event: AnalyticsEvent) {
        val eventMessage = buildString {
            append("Analytics Event: ${event.name}")
            if (event.parameters.isNotEmpty()) {
                append(" | Parameters: ${event.parameters}")
            }
        }
        logger.d { eventMessage }
        // Also log events to file
        fileLogger.log("Analytics", eventMessage)
    }

    override fun setUserId(userId: String?) {
        logger.d { "Set User ID: ${userId ?: "null (cleared)"}" }
        fileLogger.log("Analytics", "User ID set: ${userId ?: "null (cleared)"}")
    }

    override fun getFileLogger(): FileLogger = fileLogger
}