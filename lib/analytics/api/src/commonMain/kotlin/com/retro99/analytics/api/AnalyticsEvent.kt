package com.retro99.analytics.api

/**
 * Base sealed interface for all analytics events.
 * Each event has a name and optional parameters for Firebase Analytics.
 */
sealed interface AnalyticsEvent {
    val name: String
    val parameters: Map<String, Any>
        get() = emptyMap()
}

/**
 * Reader-related analytics events for tracking feature usage.
 */
sealed interface ReaderAnalyticsEvent : AnalyticsEvent {

    /**
     * Tracks when a book is opened - helps understand which books/types are popular.
     */
    data class BookOpened(
        val bookUuid: String,
        val bookType: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "book_opened"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "book_type" to bookType,
        )
    }

    /**
     * Tracks when a book is closed - helps understand reading session duration.
     */
    data class BookClosed(
        val bookUuid: String,
        val readingDurationMs: Long,
        val progressPercent: Int,
    ) : ReaderAnalyticsEvent {
        override val name: String = "book_closed"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "reading_duration_ms" to readingDurationMs,
            "progress_percent" to progressPercent,
        )
    }

    /**
     * Tracks when settings panel is opened - helps understand if settings feature is used.
     */
    data class SettingsOpened(
        val bookUuid: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "settings_opened"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
        )
    }

    /**
     * Tracks when table of contents is opened - helps understand if TOC feature is used.
     */
    data class TocOpened(
        val bookUuid: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "toc_opened"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
        )
    }

    /**
     * Tracks when audio playback is started - helps understand if audio feature is used.
     */
    data class PlaybackStarted(
        val bookUuid: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "playback_started"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
        )
    }

    /**
     * Tracks when a reader setting is changed - helps understand user preferences
     * so we can set better defaults.
     */
    data class SettingChanged(
        val settingName: String,
        val newValue: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "setting_changed"
        override val parameters: Map<String, Any> = mapOf(
            "setting_name" to settingName,
            "new_value" to newValue,
        )
    }
}

