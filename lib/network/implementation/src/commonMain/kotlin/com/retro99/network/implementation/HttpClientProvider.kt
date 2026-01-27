package com.retro99.network.implementation

import com.retro99.auth.domain.tokens.BearerTokenProvider
import com.retro99.auth.domain.tokens.BearerTokenRefresher
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class HttpClientProvider(
    @Provided private val httpFactory: HttpClientEngineFactory<*>,
    @Provided private val json: Json,
    @Provided private val tokenProvider: BearerTokenProvider,
    @Provided private val tokenRefresher: BearerTokenRefresher,
) {
    fun provide(): HttpClient {
        return HttpClient(httpFactory) {
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Application.Json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = tokenProvider.getBearerToken()
                        token?.let { BearerTokens(it, "") }
                    }
                    refreshTokens {
                        val token = tokenRefresher.refreshBearerToken()
                        token?.let { BearerTokens(it, "") }
                    }
                }
            }
        }
    }
}
