package com.retro99.books.ui.model

import kotlinx.serialization.Serializable

/**
 * Quick toggle filters for the book list.
 * These are simple boolean filters that can be toggled on/off.
 */
@Serializable
enum class BookQuickFilter {
    FAVORITES,
    IN_PROGRESS,
    CACHED,
    HAS_EBOOK,
    HAS_READALOUD,
    IN_SERIES,
    LOCAL_BOOKS,
    REMOTE_BOOKS,
}

