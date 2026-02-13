package com.retro99.analytics.implementation.di

import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.FileLogger
import com.retro99.analytics.implementation.AnalyticsManager
import com.retro99.analytics.implementation.DebugAnalyticsManager
import com.retro99.preferences.api.Preferences
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics
import dev.gitlive.firebase.crashlytics.crashlytics
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(
    includes = [
        PlatformAnalyticsModule::class,
    ],
)
@Configuration
@ComponentScan("com.retro99.analytics.implementation")
class AnalyticsModule {

    @Single
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Single
    fun provideCrashlytics(): FirebaseCrashlytics = Firebase.crashlytics

    @Single
    fun provideAnalytics(
        firebaseAnalytics: FirebaseAnalytics,
        firebaseCrashlytics: FirebaseCrashlytics,
        fileLogger: FileLogger,
        @Provided preferences: Preferences,
        @Named("isDebug") isDebug: Boolean,
    ): Analytics = if (isDebug) {
        DebugAnalyticsManager(fileLogger, preferences)
    } else {
        AnalyticsManager(
            firebaseAnalytics,
            firebaseCrashlytics,
            fileLogger,
            preferences,
        )
    }
}