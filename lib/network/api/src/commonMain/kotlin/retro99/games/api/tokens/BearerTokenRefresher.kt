package retro99.games.api.tokens

interface BearerTokenRefresher {
    suspend fun refreshBearerToken(): String?
}
