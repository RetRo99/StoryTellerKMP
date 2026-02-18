package com.retro99.home.ui.navigation

import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.model.CurrentlyReadingDomainModel

/**
 * UI model for the currently reading book.
 * Used to display the floating "Continue Reading" button.
 */
data class CurrentlyReadingUiModel(
    val serverId: String,
    val bookUuid: String,
    val bookType: BookType,
    val bookTitle: String,
    val coverUrl: String?,
    val totalProgression: Double?,
) {
    /**
     * Returns the progress as a percentage (0-100).
     */
    val progressPercent: Int
        get() = totalProgression?.let { (it * 100).toInt() } ?: 0
}

/**
 * Maps a domain model to a UI model.
 */
fun CurrentlyReadingDomainModel.toUiModel(): CurrentlyReadingUiModel {
    return CurrentlyReadingUiModel(
        serverId = serverId,
        bookUuid = bookUuid,
        bookType = bookType,
        bookTitle = bookTitle,
        coverUrl = coverUrl,
        totalProgression = totalProgression,
    )
}

