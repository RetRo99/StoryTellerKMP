package com.retro99.server.storyteller

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.server.api.QueryParamsScope
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.ServerTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray

class StorytellerNetworkClient(
    private val httpClient: HttpClient,
    private val tokenProvider: ServerTokenProvider,
    private val serverConfig: ServerConfig,
) : ServerNetworkClient {

    override val serverId: String = serverConfig.id
    override val baseUrl: String = serverConfig.baseUrl

    override suspend fun <T> getWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<T> = performRequest(typeInfo) {
        val token = tokenProvider.getToken(serverId)
        httpClient.get(buildUrl(path, queryBuilder)) {
            headers(headers)
            addAuthHeader(token)
        }
    }

    override suspend fun <T> postWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<T> = performRequest(typeInfo) {
        val token = tokenProvider.getToken(serverId)
        httpClient.post(buildUrl(path, queryBuilder)) {
            headers(headers)
            addAuthHeader(token)
            body?.let { setBody(it) }
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun <T> postFormWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        formData: Map<String, String>,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<T> = performRequest(typeInfo) {
        val token = tokenProvider.getToken(serverId)
        httpClient.submitForm(
            url = buildUrl(path, queryBuilder),
            formParameters = Parameters.build {
                formData.forEach { (key, value) -> append(key, value) }
            }
        ) {
            headers(headers)
            addAuthHeader(token)
        }
    }

    override suspend fun <T> deleteWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any?,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<T> = performRequest(typeInfo) {
        val token = tokenProvider.getToken(serverId)
        httpClient.delete(buildUrl(path, queryBuilder)) {
            headers(headers)
            addAuthHeader(token)
            body?.let { setBody(it) }
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun downloadFile(
        path: String,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val token = tokenProvider.getToken(serverId)
            val response = httpClient.get(buildUrl(path, queryBuilder)) {
                headers(headers)
                addAuthHeader(token)
            }
            if (response.status.isSuccess()) {
                val bytes = response.bodyAsChannel().readRemaining().readByteArray()
                Ok(bytes)
            } else {
                Err(AppError.NetworkError(Exception("Download failed: ${response.status}")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Err(AppError.NetworkError(e))
        }
    }

    override suspend fun downloadFileToPath(
        path: String,
        destinationPath: String,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<String> {
        // TODO: Implement file download to path
        return Err(AppError.UnknownError(NotImplementedError("File download to path not yet implemented")))
    }

    override suspend fun downloadFileToPathWithProgress(
        path: String,
        destinationPath: String,
        onProgress: suspend (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
        queryBuilder: QueryParamsScope.() -> Unit,
        headers: HeadersBuilder.() -> Unit,
    ): AppResult<String> {
        // TODO: Implement file download with progress
        return Err(AppError.UnknownError(NotImplementedError("File download with progress not yet implemented")))
    }

    override fun close() {
        // HttpClient is shared, don't close it here
    }

    private fun io.ktor.client.request.HttpRequestBuilder.addAuthHeader(token: String?) {
        token?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
        }
    }

    private fun buildUrl(path: String, queryBuilder: QueryParamsScope.() -> Unit): String {
        val scope = QueryParamsScopeImpl()
        queryBuilder(scope)

        return URLBuilder(baseUrl).apply {
            pathSegments = pathSegments + path.trimStart('/').split('/')
            scope.params.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }.buildString()
    }

    private suspend fun <T> performRequest(
        typeInfo: TypeInfo,
        block: suspend () -> io.ktor.client.statement.HttpResponse,
    ): AppResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            if (response.status.isSuccess()) {
                @Suppress("UNCHECKED_CAST")
                Ok(response.body(typeInfo) as T)
            } else {
                Err(mapHttpError(response.status.value))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Err(AppError.NetworkError(e))
        }
    }

    private fun mapHttpError(statusCode: Int): AppError = when (statusCode) {
        401 -> AppError.AuthError("Unauthorized")
        403 -> AppError.AuthError("Forbidden")
        404 -> AppError.NotFoundError("Resource not found")
        else -> AppError.NetworkError(Exception("HTTP error: $statusCode"))
    }
}

private class QueryParamsScopeImpl : QueryParamsScope {
    val params = mutableMapOf<String, String>()

    override fun param(key: String, value: String?) {
        value?.let { params[key] = it }
    }

    override fun param(key: String, value: Number?) {
        value?.let { params[key] = it.toString() }
    }

    override fun param(key: String, value: Boolean?) {
        value?.let { params[key] = it.toString() }
    }
}

