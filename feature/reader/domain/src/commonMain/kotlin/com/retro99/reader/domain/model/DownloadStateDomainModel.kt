package com.retro99.reader.domain.model

import com.retro99.base.result.AppError

/**
 * Represents the download state for a specific book and book type.
 */
sealed interface DownloadStateDomainModel {
    /**
     * No download in progress and file is not cached.
     */
    data object Idle : DownloadStateDomainModel

    /**
     * Download is currently in progress.
     *
     * @param progress Download progress from 0.0 to 1.0, or null if unknown
     */
    data class Downloading(val progress: Float? = null) : DownloadStateDomainModel

    /**
     * File has been downloaded and is cached locally.
     */
    data object Cached : DownloadStateDomainModel

    /**
     * Download failed with an error.
     */
    data class Failed(val error: AppError) : DownloadStateDomainModel
}

/**
 * Key to identify a specific download (book + type combination).
 */
data class DownloadKey(
    val bookUuid: String,
    val bookType: BookType,
)

