package com.retro99.server.api

/**
 * Factory for creating server-specific ReaderRepository implementations.
 */
interface ServerReaderRepositoryFactory {
    /**
     * The server type this factory handles.
     */
    val serverType: ServerType

    /**
     * Create a ReaderRepository for a specific server.
     */
    fun create(serverConfig: ServerConfig): ServerReaderRepository
}

