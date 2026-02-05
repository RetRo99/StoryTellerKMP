package com.retro99.network.implementation

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
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

@Single(binds = [NetworkClient::class])
class KtorNetworkClient(
    @Provided private val httpClient: HttpClient,
    @Provided private val baseUrlProvider: BaseUrlProvider,
) : NetworkClient {

    override suspend fun <T> getWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> = performRequestWithTypeInfo(typeInfo) {
        val url = buildUrl(path, queryBuilder)
        httpClient.get(url) {
            headers(headers)
        }
    }

    override suspend fun <T> postWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> = performRequestWithTypeInfo(typeInfo) {
        val url = buildUrl(path, queryBuilder)
        httpClient.post(url) {
            headers(headers)
            body?.let { setBody(it) }
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun <T> deleteWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> = performRequestWithTypeInfo(typeInfo) {
        val url = buildUrl(path, queryBuilder)
        httpClient.delete(url) {
            headers(headers)
            body?.let { setBody(it) }
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun <T> postFormWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        formData: Map<String, String>,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> = performRequestWithTypeInfo(typeInfo) {
        val url = buildUrl(path, queryBuilder)
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

    override suspend fun downloadFile(
        path: String,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(path, queryBuilder)
            val response = httpClient.get(url) {
                headers(headers)
            }

            if (response.status.isSuccess()) {
                Ok(response.bodyAsBytes())
            } else {
                handleHttpError(response)
            }
        } catch (e: Exception) {
            ensureActive()
            handleException(e)
        }
    }

    override suspend fun downloadFileToPath(
        path: String,
        destinationPath: String,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(path, queryBuilder)

            // Use prepareGet for streaming - this doesn't buffer the entire response in memory
            httpClient.prepareGet(url) {
                headers(headers)
            }.execute { response ->

                if (response.status.isSuccess()) {
                    val channel = response.bodyAsChannel()
                    writeChannelToFile(channel, destinationPath)
                    Ok(destinationPath)
                } else {
                    handleHttpError(response)
                }
            }
        } catch (e: Exception) {
            ensureActive()
            handleException(e)
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
        block: suspend () -> HttpResponse
    ): AppResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            handleResponseWithTypeInfo(response, typeInfo)
        } catch (e: Exception) {
            ensureActive()
            handleException(e)
        }
    }

    private suspend fun <T> handleResponseWithTypeInfo(
        response: HttpResponse,
        typeInfo: TypeInfo
    ): AppResult<T> {
        return if (response.status.isSuccess()) {
            parseSuccessResponseWithTypeInfo(response, typeInfo)
        } else {
            handleHttpError(response)
        }
    }

    private suspend fun <T> parseSuccessResponseWithTypeInfo(
        response: HttpResponse,
        typeInfo: TypeInfo
    ): AppResult<T> {
        return try {
            Ok(response.body(typeInfo))
        } catch (e: Exception) {
            Err(
                AppError.ApiError(
                    code = 0,
                    message = "Failed to parse response: ${e.message}"
                )
            )
        }
    }

    private suspend fun handleHttpError(response: HttpResponse): AppResult<Nothing> {
        val errorBody = response.bodyAsText()
        return when (val errorCode = response.status.value) {
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

    private fun handleException(e: Exception): AppResult<Nothing> {
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

    private fun handleNetworkException(e: Exception): AppResult<Nothing> {
        val isConnectivity = e.message?.let { message ->
            message.contains("unable to resolve host", ignoreCase = true) ||
                    message.contains("host not found", ignoreCase = true) ||
                    message.contains("network is unreachable", ignoreCase = true) ||
                    message.contains("connection refused", ignoreCase = true)
        } == true

        return Err(
            AppError.NetworkError(
                throwable = e,
                isConnectivity = isConnectivity
            )
        )
    }

    override fun close() {
        httpClient.close()
    }
}