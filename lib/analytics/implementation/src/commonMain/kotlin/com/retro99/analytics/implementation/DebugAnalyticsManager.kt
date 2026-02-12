package com.retro99.analytics.implementation

import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.AnalyticsEvent

class DebugAnalyticsManager : Analytics {

    private val logger = Logger.withTag("DebugAnalyticsManager")

    override fun logException(throwable: Throwable, message: String?) {
        logger.e(throwable) {
            if (message.isNullOrEmpty()) {
                "Exception occurred"
            } else {
                "Exception occurred with message: $message"
            }
        }
    }

    override fun logEvent(event: AnalyticsEvent) {
        logger.d {
            buildString {
                append("Analytics Event: ${event.name}")
                if (event.parameters.isNotEmpty()) {
                    append(" | Parameters: ${event.parameters}")
                }
            }
        }
    }
}