package com.retro99.analytics.implementation

import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.AnalyticsEvent

class WasmAnalyticsManager : Analytics {
    override fun logException(throwable: Throwable, message: String?) {
        println("[Analytics] ${message ?: ""} ${throwable.message}")
    }

    override fun logEvent(event: AnalyticsEvent) {
        println("[Analytics] Event: ${event.name} ${event.parameters}")
    }

    override fun setUserId(userId: String?) {
    }
}
