package retro99.games.api

import com.retro99.base.result.AppResult
import io.ktor.http.HeadersBuilder
import kotlin.reflect.KClass

interface NetworkClient {
    suspend fun <T : Any> getWithClass(
        path: String,
        type: KClass<T>,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {}
    ): AppResult<T>

    suspend fun <T : Any> postWithClass(
        path: String,
        type: KClass<T>,
        body: Any? = null,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {}
    ): AppResult<T>

    suspend fun <T : Any> deleteWithClass(
        path: String,
        type: KClass<T>,
        body: Any? = null,
        queryBuilder: QueryParamsScope.() -> Unit = {},
        headers: HeadersBuilder.() -> Unit = {}
    ): AppResult<T>

    fun close()
}

// Extension functions for reified type support
suspend inline fun <reified T : Any> NetworkClient.get(
    path: String,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {}
): AppResult<T> = getWithClass(path, T::class, queryBuilder, headers)

suspend inline fun <reified T : Any> NetworkClient.post(
    path: String,
    body: Any? = null,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {}
): AppResult<T> = postWithClass(path, T::class, body, queryBuilder, headers)

suspend inline fun <reified T : Any> NetworkClient.delete(
    path: String,
    body: Any? = null,
    noinline queryBuilder: QueryParamsScope.() -> Unit = {},
    noinline headers: HeadersBuilder.() -> Unit = {}
): AppResult<T> = deleteWithClass(path, T::class, body, queryBuilder, headers)