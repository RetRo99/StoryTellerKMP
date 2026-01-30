package retro99.network.api

import com.retro99.base.result.AppResult
import io.ktor.http.HeadersBuilder
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo

interface NetworkClient {
    suspend fun <T> getWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {}
    ): AppResult<T>

    suspend fun <T> postWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any? = null,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {}
    ): AppResult<T>

    suspend fun <T> deleteWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        body: Any? = null,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {}
    ): AppResult<T>

    suspend fun <T> postFormWithTypeInfo(
        path: String,
        typeInfo: TypeInfo,
        formData: Map<String, String>,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {}
    ): AppResult<T>

    suspend fun downloadFile(
        path: String,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {},
    ): AppResult<ByteArray>

    fun close()
}

// Extension functions for reified type support - uses TypeInfo to preserve generic type information
suspend inline fun <reified T> NetworkClient.get(
    path: String,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {}
): AppResult<T> = getWithTypeInfo(path, typeInfo<T>(), queryBuilder, headers)

suspend inline fun <reified T> NetworkClient.post(
    path: String,
    body: Any? = null,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {}
): AppResult<T> = postWithTypeInfo(path, typeInfo<T>(), body, queryBuilder, headers)

suspend inline fun <reified T> NetworkClient.delete(
    path: String,
    body: Any? = null,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {}
): AppResult<T> = deleteWithTypeInfo(path, typeInfo<T>(), body, queryBuilder, headers)

suspend inline fun <reified T> NetworkClient.postForm(
    path: String,
    formData: Map<String, String>,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {}
): AppResult<T> = postFormWithTypeInfo(path, typeInfo<T>(), formData, queryBuilder, headers)