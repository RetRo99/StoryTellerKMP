package com.retro99.analytics.implementation

import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.AnalyticsEvent
import com.retro99.analytics.api.FileLogger
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics

class AnalyticsManager(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val fileLogger: FileLogger,
) : Analytics {

    override fun logException(throwable: Throwable, message: String?) {
        // Log to Crashlytics
        message?.let { firebaseCrashlytics.log(it) }
        firebaseCrashlytics.recordException(throwable)

        // Also log to file for user sharing
        fileLogger.logException(throwable, message)
    }

    override fun logEvent(event: AnalyticsEvent) {
        val parameters = event.parameters.takeIf { it.isNotEmpty() }
        firebaseAnalytics.logEvent(event.name, parameters)

        // Also log events to file
        val eventMessage = buildString {
            append("Event: ${event.name}")
            if (event.parameters.isNotEmpty()) {
                append(" | Parameters: ${event.parameters}")
            }
        }
        fileLogger.log("Analytics", eventMessage)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
        firebaseCrashlytics.setUserId(userId ?: "")

        // Log user ID change to file
        fileLogger.log("Analytics", "User ID set: ${userId ?: "null (cleared)"}")
    }

    override fun getFileLogger(): FileLogger = fileLogger
}