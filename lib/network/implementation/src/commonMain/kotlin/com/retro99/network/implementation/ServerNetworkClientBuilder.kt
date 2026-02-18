package com.retro99.network.implementation

import com.retro99.analytics.api.Analytics
import com.retro99.server.api.ServerNetworkClient
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.NetworkClient

/**
 * Factory for creating ServerNetworkClient instances with per-server configuration.
 * Each server gets its own NetworkClient with per-server base URL and authentication.
 *
 * This is the implementation factory - it creates the actual network client instances.
 * It's different from ServerNetworkClientFactory interface which is the per-server-type factory.
 */
@Single
class ServerNetworkClientBuilder(
    @Provided private val serverHttpClientFactory: ServerHttpClientFactory,
    @Provided private val analytics: Analytics,
) {
    /**
     * Creates a ServerNetworkClient for a specific server.
     *
     * @param serverId The unique identifier for this server
     * @param baseUrl The base URL for this server
     * @param tokenProvider A suspend function that returns the current auth token
     * @param tokenRefresher Optional suspend function to refresh the token when it expires
     */
    fun build(
        serverId: String,
        baseUrl: String,
        tokenProvider: suspend () -> String?,
        tokenRefresher: (suspend () -> String?)? = null,
    ): ServerNetworkClient {
        val httpClient = serverHttpClientFactory.create(tokenProvider, tokenRefresher)

        val networkClient = KtorNetworkClient(
            httpClient = httpClient,
            baseUrlProvider = { baseUrl },
            analytics = analytics,
        )

        return DelegatingServerNetworkClient(
            serverId = serverId,
            baseUrl = baseUrl,
            delegate = networkClient,
        )
    }
}

/**
 * ServerNetworkClient implementation that delegates to a NetworkClient.
 */
private class DelegatingServerNetworkClient(
    override val serverId: String,
    override val baseUrl: String,
    private val delegate: NetworkClient,
) : ServerNetworkClient, NetworkClient by delegate

