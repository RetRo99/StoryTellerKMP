package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.data.model.PersonApiModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.NetworkClient
import retro99.network.api.get

@Factory(binds = [AuthorsRemoteSource::class])
internal class AuthorsRemoteDataSource(
    @Provided private val networkClient: NetworkClient,
) : AuthorsRemoteSource {

    override suspend fun getAuthors(): AppResult<List<PersonApiModel>> {
        return networkClient.get(path = "/api/v2/creators")
    }
}

