package com.retro99.auth.domain.tokens

interface BearerTokenRefresher {
    suspend fun refreshBearerToken(): String?
}

