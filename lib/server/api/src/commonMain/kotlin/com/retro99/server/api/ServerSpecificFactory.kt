package com.retro99.server.api

/**
 * Base interface for server-specific factories.
 * 
 * Each implementation handles a specific server type and knows how to create
 * the appropriate repository for that server.
 *
 * @param T The type of repository this factory creates.
 */
interface ServerSpecificFactory<T> {
    /**
     * The server type this factory handles.
     */
    val serverType: ServerType

    /**
     * Create a repository for a specific server.
     */
    fun create(serverConfig: ServerConfig): T
}

