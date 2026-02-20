package com.retro99.books.ui.model

import com.retro99.reader.domain.model.BookProgressInfoDomainModel

/**
 * UI model for book progress and cache information.
 */
data class BookProgressInfoUiModel(
    val bookUuid: String,
    /**
     * Total reading progress as a value between 0.0 and 1.0.
     * Null if no progress has been recorded.
     */
    val totalProgression: Double?,
    /**
     * Whether any media type is cached locally.
     */
    val hasAnyCached: Boolean,
) {
    /**
     * Returns the progress as a percentage (0-100).
     */
    val progressPercent: Int
        get() = ((totalProgression ?: 0.0) * 100).toInt()
}

fun BookProgressInfoDomainModel.toUiModel(): BookProgressInfoUiModel {
    return BookProgressInfoUiModel(
        bookUuid = bookUuid,
        totalProgression = totalProgression,
        hasAnyCached = hasAnyCached,
    )
}

