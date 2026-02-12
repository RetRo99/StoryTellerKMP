package com.retro99.analytics.api

interface Analytics {
    /**
     * Logs an exception to crashlytics with an optional context message.
     */
    fun logException(throwable: Throwable, message: String?)

    /**
     * Logs an analytics event for tracking user behavior and app usage.
     *
     * @param event The analytics event to log
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * Sets the user ID for analytics tracking.
     * This helps track user journeys across sessions.
     *
     * @param userId The user identifier (should be hashed for privacy), or null to clear
     */
    fun setUserId(userId: String?)
}