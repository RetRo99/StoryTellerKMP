package com.retro99.server.implementation

import com.retro99.server.api.BooksRepositoryFactory
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerBooksRepositoryFactory
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Composite factory for creating BooksRepository instances.
 */
@Single(binds = [BooksRepositoryFactory::class])
class CompositeServerBooksRepositoryFactory(
    @Provided factories: List<ServerBooksRepositoryFactory>,
) : CompositeRepositoryFactory<ServerBooksRepositoryFactory, ServerBooksRepository>(factories),
    BooksRepositoryFactory {

    override val factoryName: String = "books factory"
}

