package com.retro99.server.implementation

import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerBooksRepositoryFactory
import com.retro99.server.api.ServerRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [AuthenticatedRepositoryProvider::class])
class AuthenticatedRepositoryProviderImpl(
    private val serverRegistry: ServerRegistry,
    private val repositoryFactory: ServerBooksRepositoryFactory,
) : AuthenticatedRepositoryProvider {

    override fun observeBooksRepositories(): Flow<List<ServerBooksRepository>> {
        return serverRegistry.observeAuthenticatedServers().map { servers ->
            servers.map { server -> repositoryFactory.create(server) }
        }
    }

    override suspend fun getBooksRepositories(): List<ServerBooksRepository> {
        val servers = serverRegistry.getAuthenticatedServers()
        return servers.map { repositoryFactory.create(it) }
    }

    override suspend fun getBooksRepository(serverId: String): ServerBooksRepository? {
        if (!serverRegistry.isAuthenticated(serverId)) {
            return null
        }
        val server = serverRegistry.getServer(serverId) ?: return null
        return repositoryFactory.create(server)
    }

    override suspend fun getActiveBooksRepository(): ServerBooksRepository? {
        val activeServer = serverRegistry.getActiveServer() ?: return null
        if (!serverRegistry.isAuthenticated(activeServer.id)) {
            return null
        }
        return repositoryFactory.create(activeServer)
    }
}

