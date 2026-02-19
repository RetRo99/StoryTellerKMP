package com.retro99.server.implementation

import com.retro99.server.api.SeriesRepositoryFactory
import com.retro99.server.api.ServerSeriesRepository
import com.retro99.server.api.ServerSeriesRepositoryFactory
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Composite factory for creating SeriesRepository instances.
 */
@Single(binds = [SeriesRepositoryFactory::class])
class CompositeServerSeriesRepositoryFactory(
    @Provided factories: List<ServerSeriesRepositoryFactory>,
) : CompositeRepositoryFactory<ServerSeriesRepositoryFactory, ServerSeriesRepository>(factories),
    SeriesRepositoryFactory {

    override val factoryName: String = "series factory"
}

