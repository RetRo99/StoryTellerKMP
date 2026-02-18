package com.retro99.server.local

import com.retro99.books.data.source.ImportedBooksLocalSource
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerBooksRepositoryFactory
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Factory for creating LocalBooksRepository instances.
 */
@Single
class LocalBooksRepositoryFactory(
    @Provided private val localSource: ImportedBooksLocalSource,
) : ServerBooksRepositoryFactory {

    override val serverType: ServerType = ServerType.Local

    override fun create(serverConfig: ServerConfig): ServerBooksRepository {
        require(serverConfig.type == ServerType.Local) {
            "LocalBooksRepositoryFactory can only create repositories for Local servers"
        }
        return LocalBooksRepository(localSource, serverConfig.id)
    }
}

