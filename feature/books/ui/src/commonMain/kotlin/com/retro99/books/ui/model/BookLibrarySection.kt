package com.retro99.books.ui.model

import kotlinx.serialization.Serializable

@Serializable
enum class BookLibrarySection {
    ALL,
    IN_PROGRESS,
    DOWNLOADED,
    FAVORITES,
    READ_ALOUD,
    LOCAL,
}

