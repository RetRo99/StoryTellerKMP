package com.retro99.reader.ui.media

import android.net.Uri
import android.util.Log
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
import org.koin.core.annotation.Single
import org.readium.r2.shared.util.toUrl

private const val TAG = "DynamicPubDataSource"

/**
 * A DataSource.Factory that allows switching the active Publication at runtime.
 *
 * This is used for headless playback where a single ExoPlayer instance needs to
 * play audio from different EPUB files without being recreated. When a new book
 * is selected in Android Auto, call [setPublication] to update the source.
 */
@Single
@OptIn(UnstableApi::class)
class DynamicPublicationDataSourceFactory : DataSource.Factory {

    @Volatile
    private var currentPublication: Publication? = null

    /**
     * Sets the current Publication. All DataSources created after this call
     * will use this Publication to read audio files.
     *
     * @param publication The Publication to read audio from, or null to clear
     */
    fun setPublication(publication: Publication?) {
        Log.d(TAG, "setPublication: ${publication?.metadata?.title}")
        currentPublication = publication
    }

    override fun createDataSource(): DataSource {
        val publication = currentPublication
        return if (publication != null) {
            Log.d(TAG, "Creating DataSource for: ${publication.metadata.title}")
            DynamicPublicationDataSource(publication)
        } else {
            Log.w(TAG, "No publication set, creating fallback DataSource")
            FallbackDataSource()
        }
    }
}

/**
 * DataSource that reads audio from a Readium Publication.
 * Similar to [PublicationDataSource] but created by [DynamicPublicationDataSourceFactory].
 */
@OptIn(UnstableApi::class)
private class DynamicPublicationDataSource(
    private val publication: Publication,
) : BaseDataSource(/* isNetwork = */ true) {

    private data class OpenedResource(
        val resource: Resource,
        val uri: Uri,
        var position: Long,
        var remaining: Long,
    )

    private var openedResource: OpenedResource? = null
    private val cachedLengths: MutableMap<String, Long> = mutableMapOf()

    override fun open(dataSpec: DataSpec): Long {
        Log.d(TAG, "open: ${dataSpec.uri}")

        val link = dataSpec.uri.toUrl()
            ?.let { publication.linkWithHref(it) }
            ?: throw IllegalStateException(
                "Can't find a [Link] for URI: ${dataSpec.uri}. " +
                        "Make sure you only request resources declared in the manifest."
            )

        val resource = publication.get(link)
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
        val length = runBlocking { resource.length() }.getOrNull() ?: return null
        cachedLengths[uri.toString()] = length
        return length
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        val opened = openedResource ?: return RESULT_END_OF_INPUT
        if (opened.remaining == 0L) return RESULT_END_OF_INPUT

        val bytesToRead = length.toLong().coerceAtMost(opened.remaining)

        try {
            val data = runBlocking {
                opened.resource
                    .read(range = opened.position until (opened.position + bytesToRead))
                    .mapFailure { ReadException(it) }
                    .getOrThrow()
            }

            if (data.isEmpty()) return RESULT_END_OF_INPUT

            data.copyInto(buffer, offset, 0, data.size)
            opened.position += data.size
            opened.remaining -= data.size
            return data.size
        } catch (e: InterruptedException) {
            return 0
        }
    }

    override fun getUri(): Uri? = openedResource?.uri

    override fun close() {
        openedResource?.resource?.close()
        openedResource = null
    }
}

/**
 * Fallback DataSource that fails immediately. Used when no Publication is set.
 */
@OptIn(UnstableApi::class)
private class FallbackDataSource : BaseDataSource(false) {
    override fun open(dataSpec: DataSpec): Long {
        throw IllegalStateException("No Publication set in DynamicPublicationDataSourceFactory")
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int = RESULT_END_OF_INPUT
    override fun getUri(): Uri? = null
    override fun close() {}
}

