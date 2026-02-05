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

