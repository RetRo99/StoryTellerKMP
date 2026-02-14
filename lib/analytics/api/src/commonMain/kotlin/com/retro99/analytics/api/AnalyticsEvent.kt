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

    /**
     * Tracks when a settings section is expanded - helps understand which sections
     * are most used so we can prioritize their position.
     */
    data class SettingsSectionExpanded(
        val sectionName: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "settings_section_expanded"
        override val parameters: Map<String, Any> = mapOf(
            "section_name" to sectionName,
        )
    }

    /**
     * Tracks when a book fails to open - helps identify and fix publication issues.
     */
    data class BookOpenFailed(
        val bookUuid: String,
        val bookType: String,
        val errorMessage: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "book_open_failed"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "book_type" to bookType,
            "error_message" to errorMessage,
        )
    }

    /**
     * Tracks when a ReadAloud book is opened but has no media overlays.
     * This indicates a content issue that should be investigated.
     */
    data class ReadAloudMissingMediaOverlays(
        val bookUuid: String,
    ) : ReaderAnalyticsEvent {
        override val name: String = "readaloud_missing_media_overlays"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
        )
    }
}

