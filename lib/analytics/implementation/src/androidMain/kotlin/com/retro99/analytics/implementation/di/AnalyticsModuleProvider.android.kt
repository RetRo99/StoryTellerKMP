package com.retro99.analytics.implementation.di

import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.FileLogger
import com.retro99.analytics.implementation.AnalyticsManager
import com.retro99.preferences.api.Preferences
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics
import dev.gitlive.firebase.crashlytics.crashlytics

internal actual fun createProductionAnalytics(
    fileLogger: FileLogger,
    preferences: Preferences,
): Analytics {
    val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    val firebaseCrashlytics: FirebaseCrashlytics = Firebase.crashlytics
    return AnalyticsManager(
        firebaseAnalytics = firebaseAnalytics,
        firebaseCrashlytics = firebaseCrashlytics,
        fileLogger = fileLogger,
        preferences = preferences,
    )
}
