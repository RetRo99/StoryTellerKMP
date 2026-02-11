package com.retro99.network.implementation

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

private const val DOWNLOAD_BUFFER_SIZE = 64 * 1024 // 64KB buffer for faster downloads
private const val PROGRESS_UPDATE_THRESHOLD = 256 * 1024L // Update progress every 256KB

/**
 * Android implementation of streaming file write.
 * Reads from the ByteReadChannel in chunks and writes directly to disk.
 */
internal actual suspend fun writeChannelToFile(channel: ByteReadChannel, destinationPath: String) {
    val file = File(destinationPath)
    file.parentFile?.mkdirs()

    BufferedOutputStream(FileOutputStream(file), DOWNLOAD_BUFFER_SIZE).use { outputStream ->
        val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
        while (!channel.isClosedForRead) {
            val bytesRead = channel.readAvailable(buffer)
            if (bytesRead > 0) {
                outputStream.write(buffer, 0, bytesRead)
            }
        }
    }
}

/**
 * Android implementation of streaming file write with progress reporting.
 * Reads from the ByteReadChannel in chunks and writes directly to disk.
 * Progress is throttled to reduce callback overhead.
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
    BufferedOutputStream(FileOutputStream(file), DOWNLOAD_BUFFER_SIZE).use { outputStream ->
        val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
        while (!channel.isClosedForRead) {
            val bytesRead = channel.readAvailable(buffer)
            if (bytesRead > 0) {
                outputStream.write(buffer, 0, bytesRead)
                bytesWritten += bytesRead

                // Throttle progress updates to reduce overhead
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

