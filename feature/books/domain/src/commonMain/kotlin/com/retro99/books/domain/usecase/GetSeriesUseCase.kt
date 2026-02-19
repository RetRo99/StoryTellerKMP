package com.retro99.books.domain.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map as resultMap
import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.SeriesDomainModel
import com.retro99.server.api.AuthenticatedRepositoryProvider
import com.retro99.server.api.ServerSeries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map as flowMap
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for getting all series from all authenticated servers.
 * Automatically updates when servers are added/removed or auth state changes.
 */
@Factory
class GetSeriesUseCase(
    @Provided private val repositoryProvider: AuthenticatedRepositoryProvider,
) {
    operator fun invoke(): Flow<AppResult<List<SeriesDomainModel>>> {
        return repositoryProvider.observeSeriesRepositories()
            .flatMapLatest { repositories ->
                val flows = repositories.map { repo ->
                    repo.getSeries().mapToFlow { series ->
                        series.map { it.toSeriesDomainModel() }
                    }
                }

                if (flows.isEmpty()) {
                    flowOf(Ok(emptyList()))
                } else {
                    combine(flows) { results ->
                        val allSeries = results.flatMap { it.getOrElse { emptyList() } }
                        Ok(allSeries.sortedBy { it.name.lowercase() })
                    }
                }
            }
    }

    private fun <T, R> Flow<AppResult<T>>.mapToFlow(
        transform: (T) -> R
    ): Flow<AppResult<R>> = this.flowMap { result: AppResult<T> ->
        result.resultMap { value: T -> transform(value) }
    }
}

/**
 * Maps a ServerSeries to SeriesDomainModel.
 */
private fun ServerSeries.toSeriesDomainModel(): SeriesDomainModel {
    return SeriesDomainModel(
        uuid = uuid,
        name = name,
        featured = featured,
        position = position,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

