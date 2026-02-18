package com.retro99.server.api

/**
 * Factory for creating server-specific BooksRepository implementations.
 */
interface ServerBooksRepositoryFactory {
    /**
     * The server type this factory handles.
     */
    val serverType: ServerType

    /**
     * Create a BooksRepository for a specific server.
     */
    fun create(serverConfig: ServerConfig): ServerBooksRepository
}

