package com.retro99.base.ui.sharing

/**
 * Interface for sharing files with platform-specific implementations.
 * Uses expect/actual pattern for Android and iOS.
 */
interface FileSharer {

    /**
     * Shares a file at the given path using the platform's native sharing mechanism.
     *
     * @param filePath The absolute path to the file to share
     * @param mimeType The MIME type of the file (e.g., "text/plain")
     * @param title Optional title for the share dialog
     */
    fun shareFile(filePath: String, mimeType: String, title: String? = null)
}

