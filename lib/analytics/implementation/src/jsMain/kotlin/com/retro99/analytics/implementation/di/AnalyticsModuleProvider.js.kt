package com.retro99.analytics.implementation.di

import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.FileLogger
import com.retro99.analytics.implementation.WasmAnalyticsManager
import com.retro99.preferences.api.Preferences

internal actual fun createProductionAnalytics(
    fileLogger: FileLogger,
    preferences: Preferences,
): Analytics = WasmAnalyticsManager()
