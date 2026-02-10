package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.TocItemUiModel
import org.readium.r2.shared.publication.Link
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
     * The table of contents for this publication.
     * Converts Readium's Link-based TOC to a flat list with level information.
     */
    actual val tableOfContents: List<TocItemUiModel>
        get() = publication.tableOfContents.flatMapIndexed { _, link ->
            link.toTocItems(level = 0)
        }

    private fun Link.toTocItems(level: Int): List<TocItemUiModel> {
        val item = TocItemUiModel(
            href = href.toString(),
            title = title ?: href.toString(),
            level = level,
            children = children.flatMap { it.toTocItems(level + 1) },
        )
        // Return flat list: this item followed by flattened children
        return listOf(item) + children.flatMap { it.toTocItems(level + 1) }
    }
}

