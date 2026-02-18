package com.retro99.server.storyteller

import com.github.michaelbull.result.map
import com.retro99.base.result.AppResult
import com.retro99.server.api.ServerBook
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.api.get
import com.retro99.server.storyteller.model.StorytellerBookApiModel
import com.retro99.server.storyteller.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Storyteller implementation of ServerBooksRepository.
 * Fetches books from a Storyteller server using the v2 API.
 */
class StorytellerBooksRepository(
    private val networkClient: ServerNetworkClient,
) : ServerBooksRepository {

    override val serverId: String = networkClient.serverId

    override fun getBooks(): Flow<AppResult<List<ServerBook>>> = flow {
        val result = networkClient.get<List<StorytellerBookApiModel>>(
            path = "/api/v2/books"
        ).map { books ->
            books.map { it.toDomain(serverId, networkClient.baseUrl) }
        }
        emit(result)
    }

    override fun getBook(uuid: String): Flow<AppResult<ServerBook>> = flow {
        val result = networkClient.get<StorytellerBookApiModel>(
            path = "/api/v2/books/$uuid"
        ).map { book ->
            book.toDomain(serverId, networkClient.baseUrl)
        }
        emit(result)
    }

    override suspend fun searchBooks(query: String): AppResult<List<ServerBook>> {
        return networkClient.get<List<StorytellerBookApiModel>>(
            path = "/api/v2/books",
            queryBuilder = {
                param("search", query)
            }
        ).map { books ->
            books.map { it.toDomain(serverId, networkClient.baseUrl) }
        }
    }
}

