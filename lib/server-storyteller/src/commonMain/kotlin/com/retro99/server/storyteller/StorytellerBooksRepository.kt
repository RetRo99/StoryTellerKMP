package com.retro99.server.storyteller

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.map
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.base.result.mapCatching
import com.retro99.server.api.ServerBook
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.storyteller.model.StorytellerBookApiModel
import com.retro99.server.storyteller.model.toDomain
import com.retro99.server.storyteller.source.ServerBooksLocalSource
import kotlinx.coroutines.flow.Flow
import retro99.network.api.get

/**
 * Storyteller implementation of ServerBooksRepository.
 * Fetches books from a Storyteller server using the v2 API.
 * Uses local caching with cachedRemoteFlow pattern.
 */
class StorytellerBooksRepository(
    private val networkClient: ServerNetworkClient,
    private val localSource: ServerBooksLocalSource,
) : ServerBooksRepository, BaseRepository {

    override val serverId: String = networkClient.serverId
    private val baseUrl: String? = networkClient.baseUrl

    override fun getBooks(): Flow<AppResult<List<ServerBook>>> {
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getBooks(serverId).mapCatching { books ->
                    // Re-apply cover URLs since they're not stored in cache
                    books?.map { it.withCoverUrl(baseUrl) }
                }
            },
            remoteSource = {
                networkClient.get<List<StorytellerBookApiModel>>(
                    path = "/api/v2/books"
                ).map { books ->
                    books.map { it.toDomain(serverId, baseUrl) }
                        .sortedBy { it.title.lowercase() }
                }
            },
            saveToCache = { books ->
                localSource.saveBooks(serverId, books)
            },
        )
    }

    override fun getBook(uuid: String): Flow<AppResult<ServerBook>> {
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getBook(serverId, uuid).mapCatching { book ->
                    book?.withCoverUrl(baseUrl)
                }
            },
            remoteSource = {
                networkClient.get<StorytellerBookApiModel>(
                    path = "/api/v2/books/$uuid"
                ).map { book ->
                    book.toDomain(serverId, baseUrl)
                }
            },
            saveToCache = { book ->
                localSource.saveBook(serverId, book)
            },
        )
    }

    override suspend fun saveBook(book: ServerBook): CompletableResult {
        // TODO: Implement upload to Storyteller server when API supports it
        return Err(AppError.UnknownError(NotImplementedError("Uploading books to Storyteller is not yet supported")))
    }

    override suspend fun searchBooks(query: String): AppResult<List<ServerBook>> {
        return networkClient.get<List<StorytellerBookApiModel>>(
            path = "/api/v2/books",
            queryBuilder = {
                "search" to query
            }
        ).map { books ->
            books.map { it.toDomain(serverId, baseUrl) }
        }
    }

    /**
     * Re-apply cover URL to a cached book since URLs are not stored in cache.
     */
    private fun ServerBook.withCoverUrl(baseUrl: String?): ServerBook {
        return if (coverUrl == null && baseUrl != null) {
            copy(coverUrl = com.retro99.base.url.CoverUrlBuilder.buildCoverUrl(baseUrl, uuid))
        } else {
            this
        }
    }
}

