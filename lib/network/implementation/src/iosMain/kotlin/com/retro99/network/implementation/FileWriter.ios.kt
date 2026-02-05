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

private const val BUFFER_SIZE = 8 * 1024 // 8KB buffer

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

