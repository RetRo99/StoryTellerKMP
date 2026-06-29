package com.retro99.server.api

/**
 * Factory for creating server-scoped network clients.
 */
interface ServerNetworkClientFactory {
    /**
     * The server type this factory handles.
     */
    val serverType: ServerType

    /**
     * Create a network client for a specific server.
     */
    fun create(serverConfig: ServerConfig): ServerNetworkClient
}

