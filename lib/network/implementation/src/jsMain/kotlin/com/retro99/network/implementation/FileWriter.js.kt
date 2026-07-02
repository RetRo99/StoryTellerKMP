package com.retro99.network.implementation

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable

internal actual suspend fun writeChannelToFile(channel: ByteReadChannel, destinationPath: String) {
    val buffer = ByteArray(8192)
    while (true) {
        val read = channel.readAvailable(buffer)
        if (read <= 0) break
    }
}

internal actual suspend fun writeChannelToFileWithProgress(
    channel: ByteReadChannel,
    destinationPath: String,
    totalBytes: Long?,
    onProgress: suspend (bytesWritten: Long, totalBytes: Long?) -> Unit,
) {
    val buffer = ByteArray(8192)
    var bytesWritten = 0L
    while (true) {
        val read = channel.readAvailable(buffer)
        if (read <= 0) break
        bytesWritten += read
        onProgress(bytesWritten, totalBytes)
    }
}
