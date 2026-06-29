package com.retro99.server.api

/**
 * Abstraction for obtaining network clients scoped to a particular server.
 *
 * Implemented by the composite in `lib/server/implementation`; consumers
 * (feature/lib modules) should depend on this interface rather than the
 * concrete class so they only need `lib/server/api` as a dependency.
 */
interface ServerNetworkClientProvider {

    /**
     * Create a network client for the supplied [serverConfig].
     *
     * @throws IllegalArgumentException if no factory is registered for the
     *   server's [ServerConfig.type].
     */
    fun create(serverConfig: ServerConfig): ServerNetworkClient

    /**
     * Create a network client for the server identified by [serverId], or
     * `null` if no such server is registered.
     *
     * This lets callers target a specific server (e.g. for downloads kicked
     * off from a server-scoped screen) without having to look up the
     * [ServerConfig] themselves.
     */
    suspend fun createForServerId(serverId: String): ServerNetworkClient?
}
