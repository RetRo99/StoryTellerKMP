package com.retro99.reader.ui.service

import kotlinx.coroutines.flow.StateFlow

/**
 * Service responsible for managing EPUB publication lifecycle.
 *
 * This service handles opening and closing EPUB files. It is designed to be
 * injected into the ViewModel layer, separating publication management from
 * navigation concerns.
 *
 * Platform implementations provide access to the underlying publication object
 * through platform-specific methods (e.g., `getPublication()` on Android).
 */
interface EpubPublicationService {

    /**
     * Whether the publication is ready for reading.
     */
    val isReady: StateFlow<Boolean>

    /**
     * Error message if publication opening failed, null otherwise.
     */
    val error: StateFlow<String?>

    /**
     * Opens an EPUB publication from the given file path.
     *
     * @param filePath The local file path to the EPUB file
     * @return true if the publication was opened successfully, false otherwise
     */
    suspend fun openPublication(filePath: String): Boolean

    /**
     * Closes the currently open publication and releases resources.
     */
    fun closePublication()
}

