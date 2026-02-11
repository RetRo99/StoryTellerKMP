package com.retro99.network.implementation

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.writeData

private const val BUFFER_SIZE = 64 * 1024 // 64KB buffer for faster downloads
private const val PROGRESS_UPDATE_THRESHOLD = 256 * 1024L // Update progress every 256KB

/**
 * iOS implementation of streaming file write.
 * Reads from the ByteReadChannel in chunks and writes directly to disk.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun writeChannelToFile(channel: ByteReadChannel, destinationPath: String) {
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(destinationPath)) {
        fileManager.createFileAtPath(destinationPath, contents = null, attributes = null)
    }

    val fileHandle = NSFileHandle.fileHandleForWritingAtPath(destinationPath)
        ?: throw IllegalStateException("Could not open file for writing: $destinationPath")

    try {
        val buffer = ByteArray(BUFFER_SIZE)
        while (!channel.isClosedForRead) {
            val bytesRead = channel.readAvailable(buffer)
            if (bytesRead > 0) {
                buffer.usePinned { pinned ->
                    val data = NSData.create(
                        bytes = pinned.addressOf(0),
                        length = bytesRead.toULong(),
                    )
                    fileHandle.writeData(data)
                }
            }
        }
    } finally {
        fileHandle.closeFile()
    }
}

/**
 * iOS implementation of streaming file write with progress reporting.
 * Reads from the ByteReadChannel in chunks and writes directly to disk.
 * Progress is throttled to reduce callback overhead.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun writeChannelToFileWithProgress(
    channel: ByteReadChannel,
    destinationPath: String,
    totalBytes: Long?,
    onProgress: suspend (bytesWritten: Long, totalBytes: Long?) -> Unit,
) {
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(destinationPath)) {
        fileManager.createFileAtPath(destinationPath, contents = null, attributes = null)
    }

    val fileHandle = NSFileHandle.fileHandleForWritingAtPath(destinationPath)
        ?: throw IllegalStateException("Could not open file for writing: $destinationPath")

    try {
        var bytesWritten = 0L
        var lastProgressUpdate = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        while (!channel.isClosedForRead) {
            val bytesRead = channel.readAvailable(buffer)
            if (bytesRead > 0) {
                buffer.usePinned { pinned ->
                    val data = NSData.create(
                        bytes = pinned.addressOf(0),
                        length = bytesRead.toULong(),
                    )
                    fileHandle.writeData(data)
                }
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
    } finally {
        fileHandle.closeFile()
    }
}

