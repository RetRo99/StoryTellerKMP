package retro99.network.api.tokens

interface BearerTokenProvider {
    suspend fun getBearerToken(): String?
}
