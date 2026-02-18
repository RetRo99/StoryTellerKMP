package com.retro99.server.api

/**
 * Factory for creating BooksRepository instances for any server type.
 * 
 * This is the public interface used by consumers to create repositories.
 * The implementation routes to the appropriate server-specific factory
 * based on the server type in the config.
 */
interface BooksRepositoryFactory {
    /**
     * Create a BooksRepository for a specific server.
     */
    fun create(serverConfig: ServerConfig): ServerBooksRepository
}

