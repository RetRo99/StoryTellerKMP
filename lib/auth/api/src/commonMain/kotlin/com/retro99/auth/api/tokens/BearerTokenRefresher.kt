package com.retro99.auth.api.tokens

interface BearerTokenRefresher {
    suspend fun refreshBearerToken(): String?
}
