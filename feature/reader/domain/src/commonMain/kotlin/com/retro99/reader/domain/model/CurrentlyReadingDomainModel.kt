package com.retro99.reader.domain.model

import com.retro99.books.domain.model.BookType

/**
 * Represents the currently reading book that qualifies for the "Continue Reading" feature.
 * A book qualifies when it has been read for at least the minimum required duration.
 */
data class CurrentlyReadingDomainModel(
    val bookUuid: String,
    val bookType: BookType,
    val bookTitle: String,
    val coverUrl: String?,
    val totalProgression: Double?,
)

