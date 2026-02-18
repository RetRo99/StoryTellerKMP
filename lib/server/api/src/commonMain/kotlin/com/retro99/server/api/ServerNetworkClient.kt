package com.retro99.server.api

import retro99.network.api.NetworkClient

/**
 * Network client scoped to a specific server.
 * Extends NetworkClient with server-specific properties.
 * Automatically uses the correct base URL and authentication token.
 *
 * Use extension functions from NetworkClient (get, post, delete, postForm) for making requests.
 */
interface ServerNetworkClient : NetworkClient {
    val serverId: String
    val baseUrl: String
}

