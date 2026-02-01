package com.retro99.reader.ui.publication

import com.retro99.reader.ui.bridge.EpubReaderBridge

/**
 * iOS implementation of EpubPublication.
 * Wraps the EpubReaderBridge which provides access to the Swift Readium implementation.
 */
actual class EpubPublication(
    internal val bridge: EpubReaderBridge,
) {
    /**
     * Closes the publication and releases resources.
     */
    actual fun close() {
        bridge.closePublication()
    }
}

