package com.retro99.reader.ui.media

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import kotlinx.coroutines.runBlocking
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.getOrElse
import java.io.IOException

/**
 * Custom DataSource that reads audio files from a Readium Publication container.
 * This allows ExoPlayer to play audio files embedded inside an EPUB.
 */
@OptIn(UnstableApi::class)
internal class PublicationDataSource(
    private val publication: Publication,
) : BaseDataSource(/* isNetwork = */ false) {

    private var audioBytes: ByteArray? = null
    private var bytesRemaining: Long = 0
    private var readPosition: Int = 0

    override fun open(dataSpec: DataSpec): Long {
        val uri = dataSpec.uri
        val path = uri.path ?: uri.toString()

        // Convert URI path to Readium Url
        val audioUrl = Url(path) ?: throw IOException("Invalid audio URL: $path")

        // Read the audio bytes from the publication (blocking call)
        val bytes = runBlocking {
            val resource = publication.get(audioUrl)
                ?: throw IOException("Audio resource not found: $audioUrl")
            resource.read().getOrElse {
                throw IOException("Failed to read audio: $audioUrl")
            }
        }

        audioBytes = bytes
        readPosition = dataSpec.position.toInt()
        bytesRemaining = bytes.size.toLong() - dataSpec.position

        return bytesRemaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (bytesRemaining == 0L) {
            return -1 // End of data
        }

        val bytes = audioBytes ?: return -1
        val bytesToRead = minOf(length.toLong(), bytesRemaining).toInt()

        System.arraycopy(bytes, readPosition, buffer, offset, bytesToRead)
        readPosition += bytesToRead
        bytesRemaining -= bytesToRead

        return bytesToRead
    }

    override fun getUri(): Uri? = null

    override fun close() {
        audioBytes = null
        bytesRemaining = 0
        readPosition = 0
    }

    /**
     * Factory for creating PublicationDataSource instances.
     */
    class Factory(private val publication: Publication) : DataSource.Factory {
        override fun createDataSource(): DataSource = PublicationDataSource(publication)
    }
}

