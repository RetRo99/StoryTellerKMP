package com.retro99.reader.ui.publication

/**
 * Platform-agnostic wrapper for EPUB publication.
 *
 * On Android, this wraps Readium's Publication object.
 * On iOS, this wraps the EpubReaderBridge.
 */
expect class EpubPublication {
    /**
     * Closes the publication and releases any associated resources.
     */
    fun close()
}

