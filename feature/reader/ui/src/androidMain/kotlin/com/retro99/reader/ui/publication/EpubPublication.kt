package com.retro99.reader.ui.publication

import android.graphics.Bitmap
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.TocItemUiModel
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import java.io.ByteArrayOutputStream
import kotlin.coroutines.cancellation.CancellationException

/**
 * Android implementation of EpubPublication.
 * Wraps Readium's Publication object.
 */
actual class EpubPublication(
    internal val publication: Publication,
    actual val bookUuid: String,
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
        val flattenedChildren = children.flatMap { it.toTocItems(level + 1) }
        val item = TocItemUiModel(
            href = href.toString(),
            title = title ?: href.toString(),
            level = level,
            children = flattenedChildren,
        )
        // Return flat list: this item followed by flattened children
        return listOf(item) + flattenedChildren
    }

    /**
     * Gets the cover image of the publication as a byte array.
     * Uses Readium's Publication.cover() extension function.
     * Returns null if no cover is available or if loading fails.
     *
     * The cover is compressed as PNG for best quality with transparency support.
     */
    suspend fun cover(): ByteArray? {
        return try {
            val bitmap = publication.cover() ?: return null

            // Compress to PNG byte array for Media3 notification
            ByteArrayOutputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.toByteArray()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}

