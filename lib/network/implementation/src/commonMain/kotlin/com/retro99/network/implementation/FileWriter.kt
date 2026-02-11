package com.retro99.network.implementation

import io.ktor.utils.io.ByteReadChannel

/**
 * Platform-specific function to write a ByteReadChannel to a file.
 * This streams the data directly to disk without loading it all into memory.
 *
 * @param channel The Ktor ByteReadChannel to read from
 * @param destinationPath The local file path to write to
 */
internal expect suspend fun writeChannelToFile(channel: ByteReadChannel, destinationPath: String)

/**
 * Platform-specific function to write a ByteReadChannel to a file with progress reporting.
 * This streams the data directly to disk without loading it all into memory.
 *
 * @param channel The Ktor ByteReadChannel to read from
 * @param destinationPath The local file path to write to
 * @param totalBytes The total size of the file (from Content-Length header), or null if unknown
 * @param onProgress Suspend callback invoked with bytes written so far and total bytes
 */
internal expect suspend fun writeChannelToFileWithProgress(
    channel: ByteReadChannel,
    destinationPath: String,
    totalBytes: Long?,
    onProgress: suspend (bytesWritten: Long, totalBytes: Long?) -> Unit,
)

