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
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
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
import me.tatarka.inject.annotations.Inject
import retro99.games.api.NetworkClient
import retro99.games.api.QueryParamsScope
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.reflect.KClass

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class KtorNetworkClient(
    private val httpClient: HttpClient,
) : NetworkClient {

    override suspend fun <T : Any> getWithClass(
        path: String,
        type: KClass<T>,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> = performRequest(type) {
        val url = buildUrl(path, queryBuilder)
        httpClient.get(url) {
            headers(headers)
        }
    }

    override suspend fun <T : Any> postWithClass(
        path: String,
        type: KClass<T>,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> = performRequest(type) {
        val url = buildUrl(path, queryBuilder)
        httpClient.post(url) {
            headers(headers)
            body?.let { setBody(it) }
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun <T : Any> deleteWithClass(
        path: String,
        type: KClass<T>,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit
    ): AppResult<T> = performRequest(type) {
        val url = buildUrl(path, queryBuilder)
        httpClient.delete(url) {
            headers(headers)
            body?.let { setBody(it) }
            contentType(ContentType.Application.Json)
        }
    }

    @PublishedApi
    internal fun buildUrl(path: String, queryBuilder: QueryParamsScope.() -> Unit): String {
        val queryParamsScope = QueryParamsScope()
        queryBuilder(queryParamsScope)

        val urlBuilder = URLBuilder(BASE_URL).apply {
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

    private suspend fun <T : Any> performRequest(
        type: KClass<T>,
        block: suspend () -> HttpResponse
    ): AppResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            handleResponse(response, type)
        } catch (e: Exception) {
            ensureActive()
            handleException(e)
        }
    }

    private suspend fun <T : Any> handleResponse(
        response: HttpResponse,
        type: KClass<T>
    ): AppResult<T> {
        return if (response.status.isSuccess()) {
            parseSuccessResponse(response, type)
        } else {
            handleHttpError(response)
        }
    }

    private suspend fun <T : Any> parseSuccessResponse(
        response: HttpResponse,
        type: KClass<T>
    ): AppResult<T> {
        return try {
            Ok(response.body(TypeInfo(type)))
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
        val errorCode = response.status.value

        return when (errorCode) {
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

    companion object {
        private const val BASE_URL = "https://dolphin-app-zeoxd.ondigitalocean.app"
    }
}