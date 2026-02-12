package com.retro99.base.deeplink

/**
 * Utility object for building deep link URIs.
 *
 * This provides a centralized place for deep link URI construction,
 * ensuring consistency between URI building (in reader:ui) and
 * URI parsing (in home:ui).
 *
 * URI Scheme: `parrot://`
 */
object DeepLinkUriBuilder {

    const val SCHEME = "parrot://"
    const val PATH_READER = "reader"
    const val PARAM_BOOK_UUID = "bookUuid"
    const val PARAM_BOOK_TYPE = "bookType"

    /**
     * Builds a deep link URI for the reader screen.
     *
     * @param bookUuid The unique identifier of the book
     * @param bookType The type of book as a string (e.g., "ebook", "audiobook", "readaloud")
     * @return The deep link URI string (e.g., "parrot://reader?bookUuid=xxx&bookType=readaloud")
     */
    fun buildReaderUri(bookUuid: String, bookType: String): String {
        return "$SCHEME$PATH_READER?$PARAM_BOOK_UUID=$bookUuid&$PARAM_BOOK_TYPE=$bookType"
    }
}

