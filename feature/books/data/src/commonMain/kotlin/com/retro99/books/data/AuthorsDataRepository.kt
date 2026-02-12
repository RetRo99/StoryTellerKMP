package com.retro99.books.data

import com.github.michaelbull.result.map
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppResult
import com.retro99.books.data.model.toDomain
import com.retro99.books.data.model.toLocal
import com.retro99.books.data.source.AuthorsLocalSource
import com.retro99.books.data.source.AuthorsRemoteSource
import com.retro99.books.domain.AuthorsRepository
import com.retro99.books.domain.model.PersonDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [AuthorsRepository::class])
internal class AuthorsDataRepository(
    @Provided private val remoteSource: AuthorsRemoteSource,
    @Provided private val localSource: AuthorsLocalSource,
) : AuthorsRepository, BaseRepository {

    override fun getAuthors(): Flow<AppResult<List<PersonDomainModel>>> {
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getAuthors().map { authors ->
                    authors?.map { it.toDomain() }
                }
            },
            remoteSource = {
                remoteSource.getAuthors().map { authorsList ->
                    authorsList.map { it.toDomain() }
                        .sortedBy { it.name.lowercase() }
                }
            },
            saveToCache = { domainAuthors ->
                localSource.saveAuthors(domainAuthors.map { it.toLocal() })
            },
        )
    }
}

