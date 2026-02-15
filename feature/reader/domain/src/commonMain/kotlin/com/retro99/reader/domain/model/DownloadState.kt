package com.retro99.reader.domain.model

import com.retro99.base.result.AppError
import com.retro99.books.domain.model.BookType

/**
 * Represents the download state for a specific book and book type.
 */
sealed interface DownloadState {
    /**
     * No download in progress and file is not cached.
     */
    data object Idle : DownloadState

    /**
     * Download is currently in progress.
     *
     * @param progress Download progress from 0.0 to 1.0, or null if unknown
     */
    data class Downloading(val progress: Float? = null) : DownloadState

    /**
     * File has been downloaded and is cached locally.
     */
    data object Cached : DownloadState

    /**
     * Download failed with an error.
     */
    data class Failed(val error: AppError) : DownloadState
}

/**
 * Key to identify a specific download (book + type combination).
 */
data class DownloadKey(
    val bookUuid: String,
    val bookType: BookType,
)

