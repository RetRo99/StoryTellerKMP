package com.retro99.auth.api.tokens

interface BearerTokenProvider {
    suspend fun getBearerToken(): String?
}
