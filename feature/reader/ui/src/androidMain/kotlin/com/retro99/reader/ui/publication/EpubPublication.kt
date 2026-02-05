package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import org.readium.r2.shared.publication.Publication

/**
 * Android implementation of EpubPublication.
 * Wraps Readium's Publication object.
 */
actual class EpubPublication(
    internal val publication: Publication,
    actual val initialSettings: ReaderSettingsUiModel,
    actual val bookType: BookType = BookType.EBOOK,
    val initialPosition: PositionUiModel? = null,
) {
    /**
     * Whether this publication has media overlays (audio narration).
     * Checks Readium's publication metadata for media overlay information.
     */
    actual val hasMediaOverlays: Boolean
        get() = publication.metadata.duration != null ||
                publication.resources.any { it.mediaType?.isAudio == true } ||
                publication.resources.any {
                    it.mediaType?.toString()?.contains("smil") == true ||
                            it.href.toString().endsWith(".smil")
                }

    /**
     * Closes the publication and releases resources.
     * Note: Readium's Publication doesn't require explicit cleanup,
     * but we provide this for consistency across platforms.
     */
    actual fun close() {
        // No explicit cleanup needed for Readium Publication
    }
}

