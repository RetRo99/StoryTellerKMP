package com.retro99.books.data.source

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.PersonApiModel
import com.retro99.server.api.ServerNetworkClientProvider
import com.retro99.server.api.ServerRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.get

@Factory(binds = [AuthorsRemoteSource::class])
internal class AuthorsRemoteDataSource(
    @Provided private val networkClientProvider: ServerNetworkClientProvider,
    @Provided private val serverRegistry: ServerRegistry,
) : AuthorsRemoteSource {

    private val logger = Logger.withTag("AuthorsRemoteDataSource")

    override suspend fun getAuthors(): AppResult<List<PersonApiModel>> {
        val authenticatedServers = serverRegistry.observeAuthenticatedServers().first()
        if (authenticatedServers.isEmpty()) return Ok(emptyList())

        return coroutineScope {
            val results = authenticatedServers.map { server ->
                async {
                    networkClientProvider.createForServerId(server.id)?.get<List<PersonApiModel>>(
                        path = "/api/v2/creators",
                    )
                }
            }.awaitAll()

            val combined = results.mapNotNull { it }.flatMap { result ->
                result.getOrElse { error ->
                    logger.e { "Failed to fetch authors from server: $error" }
                    emptyList()
                }
            }
            Ok(combined)
        }
    }
}
