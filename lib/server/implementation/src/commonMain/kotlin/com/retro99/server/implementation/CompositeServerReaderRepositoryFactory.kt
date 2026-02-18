package com.retro99.server.implementation

import com.retro99.server.api.ReaderRepositoryFactory
import com.retro99.server.api.ServerReaderRepository
import com.retro99.server.api.ServerReaderRepositoryFactory
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Composite factory for creating ReaderRepository instances.
 */
@Single(binds = [ReaderRepositoryFactory::class])
class CompositeServerReaderRepositoryFactory(
    @Provided factories: List<ServerReaderRepositoryFactory>,
) : CompositeRepositoryFactory<ServerReaderRepositoryFactory, ServerReaderRepository>(factories),
    ReaderRepositoryFactory {

    override val factoryName: String = "reader factory"
}

