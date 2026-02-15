package com.retro99.base.url

/**
 * Utility object for building book cover URLs.
 * Centralizes the cover URL construction logic to avoid duplication across the codebase.
 */
object CoverUrlBuilder {

    private const val COVER_PATH = "/api/v2/books"
    private const val COVER_SUFFIX = "cover"
    private const val AUDIO_QUERY_PARAM = "?audio"

    /**
     * Builds a cover URL for a book.
     *
     * @param baseUrl The base URL of the server (e.g., "https://example.com")
     * @param bookUuid The unique identifier of the book
     * @return The full cover URL, or null if baseUrl is null
     */
    fun buildCoverUrl(baseUrl: String?, bookUuid: String): String? {
        return baseUrl?.let { "$it$COVER_PATH/$bookUuid/$COVER_SUFFIX" }
    }

    /**
     * Builds an ebook cover URL for a book.
     * Currently returns the same URL as [buildCoverUrl].
     *
     * @param baseUrl The base URL of the server (e.g., "https://example.com")
     * @param bookUuid The unique identifier of the book
     * @return The full ebook cover URL, or null if baseUrl is null
     */
    fun buildEbookCoverUrl(baseUrl: String?, bookUuid: String): String? {
        return buildCoverUrl(baseUrl, bookUuid)
    }

    /**
     * Builds an audiobook cover URL for a book.
     * Appends the "?audio" query parameter to the base cover URL.
     *
     * @param baseUrl The base URL of the server (e.g., "https://example.com")
     * @param bookUuid The unique identifier of the book
     * @return The full audiobook cover URL, or null if baseUrl is null
     */
    fun buildAudiobookCoverUrl(baseUrl: String?, bookUuid: String): String? {
        return baseUrl?.let { "$it$COVER_PATH/$bookUuid/$COVER_SUFFIX$AUDIO_QUERY_PARAM" }
    }
}

