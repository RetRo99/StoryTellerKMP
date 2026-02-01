package com.retro99.reader.ui.publication

import org.readium.r2.shared.publication.Publication

/**
 * Android implementation of EpubPublication.
 * Wraps Readium's Publication object.
 */
actual class EpubPublication(
    internal val publication: Publication,
) {
    /**
     * Closes the publication and releases resources.
     * Note: Readium's Publication doesn't require explicit cleanup,
     * but we provide this for consistency across platforms.
     */
    actual fun close() {
        // No explicit cleanup needed for Readium Publication
    }
}

