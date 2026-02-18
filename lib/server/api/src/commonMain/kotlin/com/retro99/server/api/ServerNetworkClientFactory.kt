package com.retro99.server.api

/**
 * Factory for creating server-scoped network clients.
 */
interface ServerNetworkClientFactory {
    /**
     * Create a network client for a specific server.
     */
    fun create(serverConfig: ServerConfig): ServerNetworkClient

    /**
     * Create a network client for the active server.
     * @throws IllegalStateException if no active server
     */
    suspend fun createForActiveServer(): ServerNetworkClient
}

