package com.retro99.server.audiobookshelf

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.base.result.mapCatching
import com.retro99.server.api.ServerBook
import com.retro99.server.api.ServerBooksRepository
import com.retro99.server.api.ServerNetworkClient
import com.retro99.server.audiobookshelf.model.AudiobookshelfLibraryItemApiModel
import com.retro99.server.audiobookshelf.model.AudiobookshelfLibraryItemsResponse
import com.retro99.server.audiobookshelf.model.AudiobookshelfLibraryListApiModel
import com.retro99.server.audiobookshelf.model.toDomain
import com.retro99.server.storyteller.source.ServerBooksLocalSource
import kotlinx.coroutines.flow.Flow
import retro99.network.api.get

class AudiobookshelfBooksRepository(
    private val networkClient: ServerNetworkClient,
    private val localSource: ServerBooksLocalSource,
) : ServerBooksRepository, BaseRepository {

    private val logger = Logger.withTag("AudiobookshelfBooksRepository")

    override val serverId: String = networkClient.serverId
    private val baseUrl: String? = networkClient.baseUrl

    override fun getBooks(): Flow<AppResult<List<ServerBook>>> {
        return cachedRemoteFlow(
            cacheSource = {
                localSource.getBooks(serverId).mapCatching { books ->
                    books?.map { book -> book.withCoverUrl(baseUrl) }
                }
            },
            remoteSource = {
                fetchAllLibrariesItems().map { items ->
                    items.map { item -> item.toDomain(serverId, baseUrl) }
                        .sortedBy { book -> book.title.lowercase() }
                }.onSuccess { books ->
                    logger.d { "Fetched ${books.size} books from $serverId" }
                }.onFailure { error ->
                    logger.e { "Remote fetch failed: $error" }
                }
            },
            saveToCache = { books -> localSource.saveBooks(serverId, books) },
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
                networkClient.get<AudiobookshelfLibraryItemApiModel>(
                    path = "/api/items/$uuid",
                ).map { item -> item.toDomain(serverId, baseUrl) }
            },
            saveToCache = { book -> localSource.saveBook(serverId, book) },
        )
    }

    override suspend fun saveBook(book: ServerBook): CompletableResult {
        return Err(
            AppError.UnknownError(
                NotImplementedError("Uploading books to Audiobookshelf is not yet supported"),
            ),
        )
    }

    override suspend fun searchBooks(query: String): AppResult<List<ServerBook>> {
        return fetchAllLibrariesItems(query = query).map { items ->
            items.map { item -> item.toDomain(serverId, baseUrl) }
        }
    }

    private suspend fun fetchAllLibrariesItems(
        query: String? = null,
    ): AppResult<List<AudiobookshelfLibraryItemApiModel>> {
        return networkClient.get<AudiobookshelfLibraryListApiModel>(
            path = "/api/libraries",
        ).map { libraryList ->
            libraryList.libraries.flatMap { library ->
                networkClient.get<AudiobookshelfLibraryItemsResponse>(
                    path = "/api/libraries/${library.id}/items",
                    queryBuilder = {
                        "limit" to "0"
                        if (query != null) "search" to query
                    },
                ).map { response -> response.results }.getOrElse { emptyList() }
            }
        }
    }

    private fun ServerBook.withCoverUrl(baseUrl: String?): ServerBook {
        return if (coverUrl == null && baseUrl != null) {
            copy(coverUrl = "${baseUrl.trimEnd('/')}/api/items/$uuid/cover")
        } else {
            this
        }
    }
}
