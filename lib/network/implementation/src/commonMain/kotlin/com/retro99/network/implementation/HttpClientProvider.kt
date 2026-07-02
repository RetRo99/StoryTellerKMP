package com.retro99.network.implementation

import com.retro99.analytics.api.Analytics
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

private const val CONNECT_TIMEOUT_MS = 30_000L
private const val REQUEST_TIMEOUT_MS = 5 * 60 * 1000L
private const val SOCKET_TIMEOUT_MS = 5 * 60 * 1000L

@Single
class HttpClientProvider(
    @Provided private val httpFactory: HttpClientEngineFactory<*>,
    @Provided private val json: Json,
    @Provided private val analytics: Analytics,
) {
    fun provide(): HttpClient {
        return HttpClient(httpFactory) {
            installCorsProxyIfNeeded()
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Application.Json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, request ->
                    if (cause is CancellationException) return@handleResponseExceptionWithRequest

                    val endpoint = request.url.encodedPath
                    val contextMessage = buildString {
                        append("Ktor HTTP exception")
                        append(" | endpoint=$endpoint")
                        append(" | method=${request.method.value}")
                        append(" | exceptionClass=${cause::class.simpleName}")
                    }
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
