package com.retro99.server.api

import com.retro99.base.result.AppResult
import io.ktor.http.HeadersBuilder
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo

/**
 * Network client scoped to a specific server.
 * Automatically uses the correct base URL and authentication token.
 */
interface ServerNetworkClient {
    val serverId: String
    val baseUrl: String

    suspend fun <T> getWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<T>

    suspend fun <T> postWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any? = null,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<T>

    suspend fun <T> deleteWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any? = null,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<T>

    suspend fun <T> postFormWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        formData: Map<String, String>,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<T>

    suspend fun downloadFile(
        path: String,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<ByteArray>

    suspend fun downloadFileToPath(
        path: String,
        destinationPath: String,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<String>

    suspend fun downloadFileToPathWithProgress(
        path: String,
        destinationPath: String,
        onProgress: suspend (bytesDownloaded: Long, totalBytes: Long?) -> Unit,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<String>

    fun close()
}

/**
 * Scope for building query parameters.
 */
interface QueryParamsScope {
    fun param(key: String, value: String?)
    fun param(key: String, value: Number?)
    fun param(key: String, value: Boolean?)
}

// Extension functions for reified type support
suspend inline fun <reified T> ServerNetworkClient.get(
    path: String,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {},
): AppResult<T> = getWithTypeInfo(path, typeInfo<T>(), queryBuilder, headers)

suspend inline fun <reified T> ServerNetworkClient.post(
    path: String,
    body: Any? = null,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {},
): AppResult<T> = postWithTypeInfo(path, typeInfo<T>(), body, queryBuilder, headers)

suspend inline fun <reified T> ServerNetworkClient.delete(
    path: String,
    body: Any? = null,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {},
): AppResult<T> = deleteWithTypeInfo(path, typeInfo<T>(), body, queryBuilder, headers)

suspend inline fun <reified T> ServerNetworkClient.postForm(
    path: String,
    formData: Map<String, String>,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {},
): AppResult<T> = postFormWithTypeInfo(path, typeInfo<T>(), formData, queryBuilder, headers)

