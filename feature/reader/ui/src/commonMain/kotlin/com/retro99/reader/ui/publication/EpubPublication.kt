package com.retro99.reader.ui.publication

import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.TocItemUiModel

/**
 * Platform-agnostic wrapper for EPUB publication.
 *
 * On Android, this wraps Readium's Publication object.
 * On iOS, this wraps the EpubReaderBridge.
 */
expect class EpubPublication {
    /**
     * The ID of the server this book belongs to.
     * Used for deep link navigation from audio notifications.
     */
    val serverId: String

    /**
     * The unique identifier of the book.
     * Used for deep link navigation from audio notifications.
     */
    val bookUuid: String

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

    /**
     * The table of contents for this publication.
     * Returns a flat list of TOC entries with level information for indentation.
     */
    val tableOfContents: List<TocItemUiModel>
}

