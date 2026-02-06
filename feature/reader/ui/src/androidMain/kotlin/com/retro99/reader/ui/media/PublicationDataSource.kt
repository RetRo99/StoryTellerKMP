package com.retro99.reader.ui.media

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C.LENGTH_UNSET
import androidx.media3.common.C.RESULT_END_OF_INPUT
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import kotlinx.coroutines.runBlocking
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.data.ReadError
import org.readium.r2.shared.util.data.ReadException
import org.readium.r2.shared.util.getOrThrow
import org.readium.r2.shared.util.resource.Resource
import org.readium.r2.shared.util.resource.buffered
import org.readium.r2.shared.util.toUrl

/**
 * Custom DataSource that reads audio files from a Readium Publication container.
 * This allows ExoPlayer to play audio files embedded inside an EPUB.
 *
 * Based on Readium's ExoPlayerDataSource implementation, this version:
 * - Reads data in chunks during read() instead of loading the entire file in open()
 * - Uses buffered() for better performance with deflated ZIP entries
 * - Caches content lengths to avoid repeated lookups
 */
@OptIn(UnstableApi::class)
internal class PublicationDataSource(
    private val publication: Publication,
) : BaseDataSource(/* isNetwork = */ true) {

    private data class OpenedResource(
        val resource: Resource,
        val uri: Uri,
        var position: Long,
        var remaining: Long,
    )

    private var openedResource: OpenedResource? = null

    /** Cached content lengths indexed by their URL. */
    private val cachedLengths: MutableMap<String, Long> = mutableMapOf()

    override fun open(dataSpec: DataSpec): Long {
        val link = dataSpec.uri.toUrl()
            ?.let { publication.linkWithHref(it) }
            ?: throw IllegalStateException(
                "Can't find a [Link] for URI: ${dataSpec.uri}. " +
                        "Make sure you only request resources declared in the manifest."
            )

        val resource = publication.get(link)
            // Significantly improves performance, in particular with deflated ZIP entries.
            ?.buffered(resourceLength = cachedLengths[dataSpec.uri.toString()])
            ?: throw ReadException(
                ReadError.Decoding(
                    "Can't find an entry for URI: ${dataSpec.uri}. Publication looks invalid."
                )
            )

        val bytesToRead = if (dataSpec.length != LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            val contentLength = contentLengthOf(dataSpec.uri, resource)
            if (contentLength == null) {
                LENGTH_UNSET.toLong()
            } else {
                contentLength - dataSpec.position
            }
        }

        openedResource = OpenedResource(
            resource = resource,
            uri = dataSpec.uri,
            position = dataSpec.position,
            remaining = bytesToRead,
        )

        return bytesToRead
    }

    private fun contentLengthOf(uri: Uri, resource: Resource): Long? {
        cachedLengths[uri.toString()]?.let { return it }

        val length = runBlocking { resource.length() }.getOrNull()
            ?: return null

        cachedLengths[uri.toString()] = length
        return length
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length <= 0) {
            return 0
        }

        val opened = openedResource ?: throw IllegalStateException(
            "No opened resource to read from. Did you call open()?"
        )

        if (opened.remaining == 0L) {
            return RESULT_END_OF_INPUT
        }

        val bytesToRead = length.toLong().coerceAtMost(opened.remaining)

        try {
            val data = runBlocking {
                opened.resource
                    .read(range = opened.position until (opened.position + bytesToRead))
                    .mapFailure { ReadException(it) }
                    .getOrThrow()
            }

            if (data.isEmpty()) {
                return RESULT_END_OF_INPUT
            }

            data.copyInto(
                destination = buffer,
                destinationOffset = offset,
                startIndex = 0,
                endIndex = data.size,
            )

            opened.position += data.size
            opened.remaining -= data.size
            return data.size
        } catch (e: Exception) {
            if (e is InterruptedException) {
                return 0
            }
            throw e
        }
    }

    override fun getUri(): Uri? = openedResource?.uri

    override fun close() {
        openedResource?.resource?.close()
        openedResource = null
    }

    /**
     * Factory for creating PublicationDataSource instances.
     */
    class Factory(private val publication: Publication) : DataSource.Factory {
        override fun createDataSource(): DataSource = PublicationDataSource(publication)
    }
}

