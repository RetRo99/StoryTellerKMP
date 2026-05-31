package com.retro99.network.implementation

import com.retro99.analytics.api.Analytics
import com.retro99.auth.domain.tokens.BearerTokenProvider
import com.retro99.auth.domain.tokens.BearerTokenRefresher
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpResponseValidator
import kotlin.coroutines.cancellation.CancellationException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

private const val CONNECT_TIMEOUT_MS = 30_000L // 30 seconds
private const val REQUEST_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes for large downloads
private const val SOCKET_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes for large downloads

@Single
class HttpClientProvider(
    @Provided private val httpFactory: HttpClientEngineFactory<*>,
    @Provided private val json: Json,
    @Provided private val tokenProvider: BearerTokenProvider,
    @Provided private val tokenRefresher: BearerTokenRefresher,
    @Provided private val analytics: Analytics,
) {
    fun provide(): HttpClient {
        return HttpClient(httpFactory) {
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Application.Json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
            install(Auth) {
                bearer {
                    sendWithoutRequest { true }
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
            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, request ->
                    // CancellationException is a normal part of coroutine cancellation
                    // (e.g., user navigates away, ViewModel cleared) - don't log it
                    if (cause is CancellationException) return@handleResponseExceptionWithRequest

                    // Only log endpoint path, never full URLs for privacy
                    val endpoint = request.url.encodedPath
                    val contextMessage = buildString {
                        append("Ktor HTTP exception")
                        append(" | endpoint=$endpoint")
                        append(" | method=${request.method.value}")
                        append(" | exceptionClass=${cause::class.simpleName}")
                    }
                    // Create sanitized exception to avoid exposing URLs from original message
                    val sanitizedException = Exception(
                        "HTTP exception: ${cause::class.simpleName}",
                        cause.cause,
                    )
                    analytics.logException(sanitizedException, contextMessage)
                }
            }
        }
    }
}
