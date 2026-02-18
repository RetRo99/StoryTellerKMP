package com.retro99.server.implementation

import co.touchlab.kermit.Logger
import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.BooksRepositoryFactory
import com.retro99.server.api.ReaderRepositoryFactory
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.api.ServerRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single

@Single(binds = [AuthenticatedRepositoryProvider::class])
class AuthenticatedRepositoryProviderImpl(
    private val serverRegistry: ServerRegistry,
    private val booksRepositoryFactory: BooksRepositoryFactory,
    private val readerRepositoryFactory: ReaderRepositoryFactory,
) : AuthenticatedRepositoryProvider {

    private val logger = Logger.withTag("čič-AuthenticatedRepositoryProvider")

    override fun observeBooksRepositories(): Flow<List<ServerBooksRepository>> {
        return serverRegistry.observeAuthenticatedServers()
            .onEach { servers ->
                logger.d { "observeAuthenticatedServers emitted ${servers.size} servers: ${servers.map { it.name }}" }
            }
            .map { servers ->
                servers.map { server ->
                    logger.d { "Creating repository for server: ${server.name} (${server.id})" }
                    booksRepositoryFactory.create(server)
                }
            }
    }

    override suspend fun getBooksRepositories(): List<ServerBooksRepository> {
        val servers = serverRegistry.getAuthenticatedServers()
        return servers.map { booksRepositoryFactory.create(it) }
    }

    override suspend fun getBooksRepository(serverId: String): ServerBooksRepository? {
        if (!serverRegistry.isAuthenticated(serverId)) {
            return null
        }
        val server = serverRegistry.getServer(serverId) ?: return null
        return booksRepositoryFactory.create(server)
    }

    override suspend fun getActiveBooksRepository(): ServerBooksRepository? {
        val activeServer = serverRegistry.getActiveServer() ?: return null
        if (!serverRegistry.isAuthenticated(activeServer.id)) {
            return null
        }
        return booksRepositoryFactory.create(activeServer)
    }

    override suspend fun getReaderRepository(serverId: String): ServerReaderRepository? {
        if (!serverRegistry.isAuthenticated(serverId)) {
            return null
        }
        val server = serverRegistry.getServer(serverId) ?: return null
        return readerRepositoryFactory.create(server)
    }
}

