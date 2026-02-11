package com.retro99.network.implementation

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of streaming file write.
 * Reads from the ByteReadChannel in chunks and writes directly to disk.
 */
internal actual suspend fun writeChannelToFile(channel: ByteReadChannel, destinationPath: String) {
    val file = File(destinationPath)
    file.parentFile?.mkdirs()

    FileOutputStream(file).use { outputStream ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
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
    FileOutputStream(file).use { outputStream ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (!channel.isClosedForRead) {
            val bytesRead = channel.readAvailable(buffer)
            if (bytesRead > 0) {
                outputStream.write(buffer, 0, bytesRead)
                bytesWritten += bytesRead
                onProgress(bytesWritten, totalBytes)
            }
        }
    }
}

