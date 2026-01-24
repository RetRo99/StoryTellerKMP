package retro99.games.api.tokens

interface BearerTokenProvider {
    suspend fun getBearerToken(): String?
}
