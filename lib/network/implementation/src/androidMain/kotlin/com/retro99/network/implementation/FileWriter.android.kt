package com.retro99.network.implementation

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

// 256KB buffer - optimized for low-powered e-ink devices (fewer operations = less CPU)
private const val DOWNLOAD_BUFFER_SIZE = 256 * 1024

// Update progress every 1MB - e-ink screens refresh slowly, no need for frequent updates
private const val PROGRESS_UPDATE_THRESHOLD = 1024 * 1024L

/**
 * Android implementation of streaming file write.
 * Uses FileChannel for direct I/O, minimizing CPU overhead on low-powered e-ink devices.
 */
internal actual suspend fun writeChannelToFile(channel: ByteReadChannel, destinationPath: String) {
    val file = File(destinationPath)
    file.parentFile?.mkdirs()

    RandomAccessFile(file, "rw").use { raf ->
        raf.channel.use { fileChannel ->
            val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
            val byteBuffer = ByteBuffer.wrap(buffer)
            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer)
                if (bytesRead > 0) {
                    byteBuffer.clear()
                    byteBuffer.limit(bytesRead)
                    fileChannel.write(byteBuffer)
                }
            }
        }
    }
}

/**
 * Android implementation of streaming file write with progress reporting.
 * Uses FileChannel for direct I/O, minimizing CPU overhead on low-powered e-ink devices.
 * Progress is heavily throttled since e-ink screens refresh slowly.
 */
internal actual suspend fun writeChannelToFileWithProgress(
    channel: ByteReadChannel,
    destinationPath: String,
    totalBytes: Long?,
    onProgress: suspend (bytesWritten: Long, totalBytes: Long?) -> Unit,
) {
    val file = File(destinationPath)
    file.parentFile?.mkdirs()

    var bytesWritten = 0L
    var lastProgressUpdate = 0L

    RandomAccessFile(file, "rw").use { raf ->
        raf.channel.use { fileChannel ->
            val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
            val byteBuffer = ByteBuffer.wrap(buffer)
            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer)
                if (bytesRead > 0) {
                    byteBuffer.clear()
                    byteBuffer.limit(bytesRead)
                    fileChannel.write(byteBuffer)
                    bytesWritten += bytesRead

                    // Throttle progress updates - e-ink doesn't need frequent refreshes
                    if (bytesWritten - lastProgressUpdate >= PROGRESS_UPDATE_THRESHOLD) {
                        onProgress(bytesWritten, totalBytes)
                        lastProgressUpdate = bytesWritten
                    }
                }
            }
            // Final progress update to ensure 100% is reported
            if (bytesWritten != lastProgressUpdate) {
                onProgress(bytesWritten, totalBytes)
            }
        }
    }
}

