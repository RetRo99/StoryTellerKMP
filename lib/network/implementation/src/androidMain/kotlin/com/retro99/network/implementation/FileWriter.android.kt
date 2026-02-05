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

    var totalBytesWritten = 0L
    FileOutputStream(file).use { outputStream ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (!channel.isClosedForRead) {
            val bytesRead = channel.readAvailable(buffer)
            if (bytesRead > 0) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesWritten += bytesRead
            }
        }
    }
}

