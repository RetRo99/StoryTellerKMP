package com.retro99.network.implementation

import org.koin.core.annotation.Single
import retro99.games.api.tokens.BearerTokenRefresher

/**
 * Default implementation of BearerTokenRefresher that returns null.
 * Replace with actual implementation when authentication is implemented.
 */
@Single(binds = [BearerTokenRefresher::class])
class DefaultBearerTokenRefresher : BearerTokenRefresher {
    override suspend fun refreshBearerToken(): String? = null
}

