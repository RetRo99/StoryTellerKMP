package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.model.ReaderSettingsUiModel

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
    val initialSettings: ReaderSettingsUiModel

    /**
     * The type of book (EBOOK, AUDIOBOOK, or READALOUD).
     */
    val bookType: BookType

    /**
     * Whether this publication has media overlays (audio narration).
     */
    val hasMediaOverlays: Boolean
}

