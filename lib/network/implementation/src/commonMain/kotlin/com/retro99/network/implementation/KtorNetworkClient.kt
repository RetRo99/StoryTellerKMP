package com.retro99.network.implementation

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.NetworkAnalyticsEvent
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.BaseUrlProvider
import retro99.network.api.NetworkClient
import retro99.network.api.QueryParamsScope

private const val CONNECT_TIMEOUT_MS = 30_000L // 30 seconds

@Single(binds = [NetworkClient::class])
class KtorNetworkClient(
    @Provided private val httpClient: HttpClient,
    @Provided private val baseUrlProvider: BaseUrlProvider,
    @Provided private val analytics: Analytics,
) : NetworkClient {

    override suspend fun <T> getWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> {
        val url = buildUrl(path, queryBuilder)
        return performRequestWithTypeInfo(typeInfo, url) {
            httpClient.get(url) {
                headers(headers)
            }
        }
    }

    override suspend fun <T> postWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> {
        val url = buildUrl(path, queryBuilder)
        return performRequestWithTypeInfo(typeInfo, url) {
            httpClient.post(url) {
                headers(headers)
                body?.let { setBody(it) }
                contentType(ContentType.Application.Json)
            }
        }
    }

    override suspend fun <T> deleteWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> {
        val url = buildUrl(path, queryBuilder)
        return performRequestWithTypeInfo(typeInfo, url) {
            httpClient.delete(url) {
                headers(headers)
                body?.let { setBody(it) }
                contentType(ContentType.Application.Json)
            }
        }
    }

    override suspend fun <T> postFormWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        formData: Map<String, String>,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> {
        val url = buildUrl(path, queryBuilder)
        return performRequestWithTypeInfo(typeInfo, url) {
            httpClient.submitForm(
                url = url,
                formParameters = Parameters.build {
                    formData.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            ) {
                headers(headers)
            }
        }
    }

    override suspend fun downloadFile(
        path: String,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<ByteArray> = withContext(Dispatchers.IO) {
        val url = buildUrl(path, queryBuilder)
        try {
            val response = httpClient.get(url) {
                headers(headers)
            }

            if (response.status.isSuccess()) {
                Ok(response.bodyAsBytes())
            } else {
                handleHttpError(response, url)
            }
        } catch (e: Exception) {
            ensureActive()
            handleException(e, url)
        }
    }

    override suspend fun downloadFileToPath(
        path: String,
        destinationPath: String,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val url = buildUrl(path, queryBuilder)
        try {
            // Use prepareGet for streaming - this doesn't buffer the entire response in memory
            httpClient.prepareGet(url) {
                headers(headers)
            }.execute { response ->

                if (response.status.isSuccess()) {
                    val channel = response.bodyAsChannel()
                    writeChannelToFile(channel, destinationPath)
                    Ok(destinationPath)
                } else {
                    handleHttpError(response, url)
                }
            }
        } catch (e: Exception) {
            ensureActive()
            handleException(e, url)
        }
    }

    override suspend fun downloadFileToPathWithProgress(
        path: String,
        destinationPath: String,
        onProgress: suspend (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        val url = buildUrl(path, queryBuilder)
        try {
            // Use prepareGet for streaming - this doesn't buffer the entire response in memory
            httpClient.prepareGet(url) {
                headers(headers)
            }.execute { response ->

                if (response.status.isSuccess()) {
                    val channel = response.bodyAsChannel()
                    // Get Content-Length header for progress calculation
                    val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                    writeChannelToFileWithProgress(
                        channel = channel,
                        destinationPath = destinationPath,
                        totalBytes = contentLength,
                        onProgress = onProgress,
                    )
                    Ok(destinationPath)
                } else {
                    handleHttpError(response, url)
                }
            }
        } catch (e: Exception) {
            ensureActive()
            handleException(e, url)
        }
    }

    @PublishedApi
    internal fun buildUrl(path: String, queryBuilder: QueryParamsScope.() -> Unit): String {
        val queryParamsScope = QueryParamsScope()
        queryBuilder(queryParamsScope)

        val baseUrl = baseUrlProvider.getBaseUrl()
            ?: error("Server URL not configured. Please login first.")

        val urlBuilder = URLBuilder(baseUrl).apply {
            path(path)
            queryParamsScope.params.forEach { (key, value) ->
                when (value) {
                    is String, is Number, is Boolean -> parameters.append(key, value.toString())
                    is List<*> -> value.filterNotNull()
                        .forEach { parameters.append(key, it.toString()) }

                    null -> Unit // Ignore null parameters
                    else -> parameters.append(key, value.toString()) // Default case
                }
            }
        }
        return urlBuilder.buildString()
    }

    private suspend fun <T> performRequestWithTypeInfo(
        typeInfo: TypeInfo,
        requestUrl: String,
        block: suspend () -> HttpResponse,
    ): AppResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            handleResponseWithTypeInfo(response, typeInfo, requestUrl)
        } catch (e: Exception) {
            ensureActive()
            handleException(e, requestUrl)
        }
    }

    private suspend fun <T> handleResponseWithTypeInfo(
        response: HttpResponse,
        typeInfo: TypeInfo,
        requestUrl: String,
    ): AppResult<T> {
        return if (response.status.isSuccess()) {
            parseSuccessResponseWithTypeInfo(response, typeInfo)
        } else {
            handleHttpError(response, requestUrl)
        }
    }

    private suspend fun <T> parseSuccessResponseWithTypeInfo(
        response: HttpResponse,
        typeInfo: TypeInfo
    ): AppResult<T> {
        return try {
            Ok(response.body(typeInfo))
        } catch (e: Exception) {
            analytics.logException(e, "Failed to parse response for type: ${typeInfo.type}")
            Err(
                AppError.ApiError(
                    code = 0,
                    message = "Failed to parse response: ${e.message}"
                )
            )
        }
    }

    private suspend fun handleHttpError(
        response: HttpResponse,
        requestUrl: String,
    ): AppResult<Nothing> {
        val errorBody = response.bodyAsText()
        val errorCode = response.status.value
        val error = when (errorCode) {
            in 400..499 -> handleClientError(errorCode, errorBody)
            in 500..599 -> Err(
                AppError.ApiError(
                    code = errorCode,
                    message = "Server error: $errorBody"
                )
            )

            else -> Err(
                AppError.ApiError(
                    code = errorCode,
                    message = "HTTP error $errorCode: $errorBody"
                )
            )
        }
        // Log HTTP errors for debugging with URL context
        val baseUrl = baseUrlProvider.getBaseUrl() ?: "unknown"
        analytics.logException(
            Exception("HTTP $errorCode: $errorBody"),
            buildString {
                append("HTTP error on request")
                append(" | url=$requestUrl")
                append(" | baseUrl=$baseUrl")
                append(" | statusCode=$errorCode")
            }
        )
        return error
    }

    private fun handleClientError(errorCode: Int, errorBody: String): AppResult<Nothing> {
        return when (errorCode) {
            401, 403 -> Err(
                AppError.ApiError(
                    code = errorCode,
                    message = "Authentication error: $errorBody"
                )
            )

            404 -> Err(
                AppError.ApiError(
                    code = errorCode,
                    message = "Resource not found: $errorBody"
                )
            )

            else -> Err(
                AppError.ApiError(
                    code = errorCode,
                    message = "Client error: $errorBody"
                )
            )
        }
    }

    private fun handleException(e: Exception, requestUrl: String): AppResult<Nothing> {
        val baseUrl = baseUrlProvider.getBaseUrl() ?: "unknown"
        val errorType = classifyNetworkError(e)
        val isTimeout = e is ConnectTimeoutException || e is SocketTimeoutException
        val isConnectivity = isConnectivityError(e)

        // Build detailed context message for debugging
        val contextMessage = buildString {
            append("Network request exception")
            append(" | url=$requestUrl")
            append(" | baseUrl=$baseUrl")
            append(" | errorType=$errorType")
            append(" | exceptionClass=${e::class.simpleName}")
            if (isTimeout) {
                append(" | isTimeout=true")
                append(" | connectTimeoutMs=$CONNECT_TIMEOUT_MS")
            }
            if (isConnectivity) {
                append(" | isConnectivity=true")
            }
            e.message?.let { append(" | message=$it") }
        }

        analytics.logException(e, contextMessage)

        // Also log as analytics event for tracking patterns
        val endpoint = extractEndpoint(requestUrl, baseUrl)
        analytics.logEvent(
            NetworkAnalyticsEvent.NetworkRequestFailed(
                endpoint = endpoint,
                errorType = errorType,
                isTimeout = isTimeout,
                isConnectivity = isConnectivity,
            )
        )

        return when (e) {
            is IOException,
            is ConnectTimeoutException,
            is SocketTimeoutException -> handleNetworkException(e)

            is SerializationException -> Err(
                AppError.ApiError(
                    code = 0,
                    message = "Failed to parse response: ${e.message}"
                )
            )

            else -> Err(AppError.UnknownError(e))
        }
    }

    /**
     * Extracts the API endpoint path from the full URL for analytics.
     * This removes the base URL to avoid logging sensitive server addresses.
     */
    private fun extractEndpoint(requestUrl: String, baseUrl: String): String {
        return requestUrl.removePrefix(baseUrl).takeIf { it.isNotEmpty() } ?: requestUrl
    }

    private fun isConnectivityError(e: Exception): Boolean {
        return e.message?.let { message ->
            message.contains("unable to resolve host", ignoreCase = true) ||
                message.contains("host not found", ignoreCase = true) ||
                message.contains("network is unreachable", ignoreCase = true) ||
                message.contains("connection refused", ignoreCase = true)
        } == true
    }

    private fun classifyNetworkError(e: Exception): String {
        return when (e) {
            is ConnectTimeoutException -> "connect_timeout"
            is SocketTimeoutException -> "socket_timeout"
            is IOException -> {
                val message = e.message?.lowercase() ?: ""
                when {
                    message.contains("unable to resolve host") -> "dns_resolution_failed"
                    message.contains("host not found") -> "host_not_found"
                    message.contains("network is unreachable") -> "network_unreachable"
                    message.contains("connection refused") -> "connection_refused"
                    message.contains("connection reset") -> "connection_reset"
                    message.contains("broken pipe") -> "broken_pipe"
                    message.contains("ssl") || message.contains("tls") -> "ssl_error"
                    else -> "io_error"
                }
            }
            else -> "unknown"
        }
    }

    private fun handleNetworkException(e: Exception): AppResult<Nothing> {
        return Err(
            AppError.NetworkError(
                throwable = e,
                isConnectivity = isConnectivityError(e)
            )
        )
    }

    override fun close() {
        httpClient.close()
    }
}