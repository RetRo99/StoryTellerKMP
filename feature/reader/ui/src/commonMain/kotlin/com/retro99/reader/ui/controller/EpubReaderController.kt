package com.retro99.reader.ui.controller

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific controller for managing EPUB publication lifecycle.
 *
 * This controller handles opening, closing, and managing the state of EPUB publications.
 * Each platform provides its own implementation:
 * - Android: Uses Readium's Publication
 * - iOS: Uses platform-specific reader (placeholder for now)
 *
 * The controller is scoped to the reader screen lifecycle and should be injected
 * via Koin. This approach keeps platform-specific publication objects out of the
 * domain layer while providing proper lifecycle management.
 */
interface EpubReaderController {

    /**
     * Whether a publication is currently open and ready for rendering.
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
     * @return True if the publication was opened successfully, false otherwise
     */
    suspend fun openPublication(filePath: String): Boolean

    /**
     * Closes the currently open publication and releases resources.
     * This should be called when the reader screen is disposed.
     */
    fun closePublication()
}

