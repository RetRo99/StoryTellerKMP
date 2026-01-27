package com.retro99.auth.domain.tokens

interface BearerTokenProvider {
    suspend fun getBearerToken(): String?
}

