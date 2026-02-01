package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.ReaderSettingsDomainModel

/**
 * Platform-agnostic wrapper for EPUB publication.
 *
 * On Android, this wraps Readium's Publication object.
 * On iOS, this wraps the EpubReaderBridge.
 */
expect class EpubPublication {
    /**
     * The initial settings that were used to open this publication.
     */
    val initialSettings: ReaderSettingsDomainModel

    /**
     * Closes the publication and releases any associated resources.
     */
    fun close()
}

