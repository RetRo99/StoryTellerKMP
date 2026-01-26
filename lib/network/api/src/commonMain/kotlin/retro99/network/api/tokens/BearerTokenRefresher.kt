package retro99.network.api.tokens

interface BearerTokenRefresher {
    suspend fun refreshBearerToken(): String?
}
