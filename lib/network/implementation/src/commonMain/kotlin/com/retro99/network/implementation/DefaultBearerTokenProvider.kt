package com.retro99.network.implementation

import org.koin.core.annotation.Single
import retro99.games.api.tokens.BearerTokenProvider

/**
 * Default implementation of BearerTokenProvider that returns null.
 * Replace with actual implementation when authentication is implemented.
 */
@Single(binds = [BearerTokenProvider::class])
class DefaultBearerTokenProvider : BearerTokenProvider {
    override suspend fun getBearerToken(): String? = null
}

