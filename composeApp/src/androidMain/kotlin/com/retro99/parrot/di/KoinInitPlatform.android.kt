package com.retro99.parrot.di

import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.core.KoinApplication

internal actual fun KoinApplication.configurePlatformAnalytics() {
    analytics()
}
