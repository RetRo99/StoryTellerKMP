package com.retro99.analytics.api

/**
 * Book discovery and download related analytics events.
 */
sealed interface BookAnalyticsEvent : AnalyticsEvent {

    data class BookDetailViewed(
        val bookUuid: String,
        val source: String,
    ) : BookAnalyticsEvent {
        override val name: String = "book_detail_viewed"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "source" to source,
        )
    }

    data class BookDownloadStarted(
        val bookUuid: String,
        val bookType: String,
    ) : BookAnalyticsEvent {
        override val name: String = "book_download_started"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "book_type" to bookType,
        )
    }

    data class BookDownloadCompleted(
        val bookUuid: String,
        val downloadDurationMs: Long,
    ) : BookAnalyticsEvent {
        override val name: String = "book_download_completed"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "download_duration_ms" to downloadDurationMs,
        )
    }

    data class BookDownloadFailed(
        val bookUuid: String,
        val errorType: String,
    ) : BookAnalyticsEvent {
        override val name: String = "book_download_failed"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "error_type" to errorType,
        )
    }

    data class BookDownloadCancelled(
        val bookUuid: String,
    ) : BookAnalyticsEvent {
        override val name: String = "book_download_cancelled"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
        )
    }

    /**
     * Tracks when user clicks read/play button - helps understand format preferences.
     */
    data class ReadButtonClicked(
        val bookUuid: String,
        val bookType: String,
    ) : BookAnalyticsEvent {
        override val name: String = "read_button_clicked"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "book_type" to bookType,
        )
    }

    /**
     * Tracks when user deletes cached media - helps understand storage management behavior.
     */
    data class BookCacheDeleted(
        val bookUuid: String,
        val bookType: String,
    ) : BookAnalyticsEvent {
        override val name: String = "book_cache_deleted"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "book_type" to bookType,
        )
    }

    /**
     * Tracks when user adds or removes a book from favorites.
     */
    data class FavoriteToggled(
        val bookUuid: String,
        val isFavorite: Boolean,
        val source: String,
    ) : BookAnalyticsEvent {
        override val name: String = "favorite_toggled"
        override val parameters: Map<String, Any> = mapOf(
            "book_uuid" to bookUuid,
            "is_favorite" to isFavorite,
            "source" to source,
        )
    }
}

/**
 * Authentication related analytics events.
 */
sealed interface AuthAnalyticsEvent : AnalyticsEvent {

    data class LoginAttempted(
        val serverUrlHash: String,
    ) : AuthAnalyticsEvent {
        override val name: String = "login_attempted"
        override val parameters: Map<String, Any> = mapOf(
            "server_url_hash" to serverUrlHash,
        )
    }

    data object LoginSucceeded : AuthAnalyticsEvent {
        override val name: String = "login_succeeded"
    }

    data class LoginFailed(
        val errorType: String,
    ) : AuthAnalyticsEvent {
        override val name: String = "login_failed"
        override val parameters: Map<String, Any> = mapOf(
            "error_type" to errorType,
        )
    }

    data object LogoutCompleted : AuthAnalyticsEvent {
        override val name: String = "logout_completed"
    }
}

/**
 * Navigation and screen view analytics events.
 */
sealed interface NavigationAnalyticsEvent : AnalyticsEvent {

    /**
     * Tracks when user switches between tabs - helps understand which sections are most used.
     */
    data class TabSwitched(
        val tabName: String,
    ) : NavigationAnalyticsEvent {
        override val name: String = "tab_switched"
        override val parameters: Map<String, Any> = mapOf(
            "tab_name" to tabName,
        )
    }
}

