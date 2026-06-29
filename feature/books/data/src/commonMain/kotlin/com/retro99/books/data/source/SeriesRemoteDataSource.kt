package com.retro99.books.data.source

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.SeriesApiModel
import com.retro99.server.api.ServerNetworkClientProvider
import com.retro99.server.api.ServerRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.get

@Factory(binds = [SeriesRemoteSource::class])
internal class SeriesRemoteDataSource(
    @Provided private val networkClientProvider: ServerNetworkClientProvider,
    @Provided private val serverRegistry: ServerRegistry,
) : SeriesRemoteSource {

    private val logger = Logger.withTag("SeriesRemoteDataSource")

    override suspend fun getSeries(): AppResult<List<SeriesApiModel>> {
        val authenticatedServers = serverRegistry.observeAuthenticatedServers().first()
        if (authenticatedServers.isEmpty()) return Ok(emptyList())

        return coroutineScope {
            val results = authenticatedServers.map { server ->
                async {
                    networkClientProvider.createForServerId(server.id)?.get<List<SeriesApiModel>>(
                        path = "/api/v2/series",
                    )
                }
            }.awaitAll()

            val combined = results.mapNotNull { it }.flatMap { result ->
                result.getOrElse { error ->
                    logger.e { "Failed to fetch series from server: $error" }
                    emptyList()
                }
            }
            Ok(combined)
        }
    }
}
