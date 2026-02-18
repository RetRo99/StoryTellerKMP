package com.retro99.home.ui.deeplink

import com.retro99.base.deeplink.DeepLinkUriBuilder
import com.retro99.books.domain.model.BookType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

/**
 * Represents a deep link destination that the app can navigate to.
 */
sealed interface DeepLinkDestination {
    /**
     * Navigate to the reader screen for a specific book.
     *
     * @param serverId The ID of the server the book belongs to
     * @param bookUuid The unique identifier of the book
     * @param bookType The type of book (EBOOK, AUDIOBOOK, or READALOUD)
     */
    data class Reader(
        val serverId: String,
        val bookUuid: String,
        val bookType: BookType,
    ) : DeepLinkDestination
}

/**
 * Handles deep link navigation for the app.
 *
 * This singleton acts as a bridge between Android's Intent system and Compose Navigation.
 * When a deep link URI is received (e.g., from notification click), it parses the URI
 * and emits a navigation event that can be observed by the navigation ViewModels.
 *
 * URI Scheme:
 * - `parrot://reader?bookUuid={uuid}&bookType={ebook|audiobook|readaloud}`
 *
 * Usage:
 * 1. In MainActivity, call [handleDeepLink] with the intent's data URI
 * 2. In HomeNavigationViewModel, observe [navigationEvents] and navigate accordingly
 */
@Single
class DeepLinkHandler {

    private val _navigationEvents = MutableSharedFlow<DeepLinkDestination>(
        extraBufferCapacity = 1,
    )

    /**
     * Flow of navigation events triggered by deep links.
     * Observers should navigate to the emitted destination.
     */
    val navigationEvents: SharedFlow<DeepLinkDestination> = _navigationEvents.asSharedFlow()

    /**
     * Parses a deep link URI and emits a navigation event if valid.
     *
     * @param uri The deep link URI string (e.g., "parrot://reader?bookUuid=xxx&bookType=readaloud")
     * @return true if the URI was valid and a navigation event was emitted, false otherwise
     */
    fun handleDeepLink(uri: String?): Boolean {
        if (uri == null) return false

        val destination = parseUri(uri) ?: return false
        _navigationEvents.tryEmit(destination)
        return true
    }

    /**
     * Parses a URI string into a [DeepLinkDestination].
     *
     * Supports the following URI formats:
     * - `parrot://reader?bookUuid={uuid}&bookType={type}`
     *
     * @param uri The URI string to parse
     * @return The parsed destination, or null if the URI is invalid
     */
    private fun parseUri(uri: String): DeepLinkDestination? {
        // Expected format: parrot://reader?bookUuid=xxx&bookType=readaloud
        if (!uri.startsWith(DeepLinkUriBuilder.SCHEME)) return null

        val path = uri.removePrefix(DeepLinkUriBuilder.SCHEME)
        val pathAndQuery = path.split("?", limit = 2)
        val pathSegment = pathAndQuery.getOrNull(0) ?: return null
        val queryString = pathAndQuery.getOrNull(1)

        return when (pathSegment) {
            DeepLinkUriBuilder.PATH_READER -> parseReaderDestination(queryString)
            else -> null
        }
    }

    /**
     * Parses query parameters for the reader destination.
     */
    private fun parseReaderDestination(queryString: String?): DeepLinkDestination.Reader? {
        if (queryString == null) return null

        val params = parseQueryParams(queryString)
        val serverId = params[DeepLinkUriBuilder.PARAM_SERVER_ID] ?: return null
        val bookUuid = params[DeepLinkUriBuilder.PARAM_BOOK_UUID] ?: return null
        val bookTypeValue = params[DeepLinkUriBuilder.PARAM_BOOK_TYPE] ?: return null

        val bookType = BookType.entries.find { it.value == bookTypeValue } ?: return null

        return DeepLinkDestination.Reader(
            serverId = serverId,
            bookUuid = bookUuid,
            bookType = bookType,
        )
    }

    /**
     * Parses a query string into a map of key-value pairs.
     */
    private fun parseQueryParams(queryString: String): Map<String, String> {
        return queryString.split("&")
            .mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to parts[1]
                } else {
                    null
                }
            }
            .toMap()
    }

    companion object {
        /**
         * Builds a deep link URI for the reader screen.
         *
         * @param serverId The ID of the server the book belongs to
         * @param bookUuid The unique identifier of the book
         * @param bookType The type of book
         * @return The deep link URI string
         */
        fun buildReaderUri(serverId: String, bookUuid: String, bookType: BookType): String {
            return DeepLinkUriBuilder.buildReaderUri(serverId, bookUuid, bookType.value)
        }
    }
}

