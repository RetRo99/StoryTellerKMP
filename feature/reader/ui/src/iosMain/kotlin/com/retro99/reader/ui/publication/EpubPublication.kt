package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel

/**
 * iOS implementation of EpubPublication.
 * Wraps the EpubReaderBridge which provides access to the Swift Readium implementation.
 */
actual class EpubPublication(
    internal val bridge: EpubReaderBridge,
    actual val initialSettings: ReaderSettingsUiModel,
    actual val bookType: BookType = BookType.EBOOK,
    internal val initialPosition: PositionUiModel?,
) {

    /**
     * Whether this publication has media overlays (audio narration).
     */
    actual val hasMediaOverlays: Boolean
        get() = bridge.hasMediaOverlays()

}
