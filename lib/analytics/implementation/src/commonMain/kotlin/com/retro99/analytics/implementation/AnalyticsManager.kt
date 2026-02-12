package com.retro99.analytics.implementation

import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.AnalyticsEvent
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics

class AnalyticsManager(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : Analytics {

    override fun logException(throwable: Throwable, message: String?) {
        message?.let { firebaseCrashlytics.log(it) }
        firebaseCrashlytics.recordException(throwable)
    }

    override fun logEvent(event: AnalyticsEvent) {
        val parameters = event.parameters.takeIf { it.isNotEmpty() }
        firebaseAnalytics.logEvent(event.name, parameters)
    }
}