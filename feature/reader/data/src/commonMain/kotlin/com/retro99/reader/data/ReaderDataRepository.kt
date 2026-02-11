package com.retro99.reader.data

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.retro99.analytics.api.Analytics
import com.retro99.base.repository.BaseRepository
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.data.model.toApiModel
import com.retro99.reader.data.model.toDomain
import com.retro99.reader.data.model.toLocal
import com.retro99.reader.data.source.ReaderLocalSource
import com.retro99.reader.data.source.ReaderRemoteSource
import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.PositionDomainModel
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [ReaderRepository::class])
internal class ReaderDataRepository(
    @Provided private val localSource: ReaderLocalSource,
    @Provided private val remoteSource: ReaderRemoteSource,
    @Provided private val analytics: Analytics,
) : ReaderRepository, BaseRepository {

    override suspend fun prepareEbook(
        bookUuid: String,
        ebookFilePath: String,
        bookType: BookType,
    ): AppResult<String> {
        // Return cached path if available.
        // Downloads are handled by BookDownloadManager which provides progress tracking
        // and background download support. This method should only be called after
        // the download is complete (i.e., when DownloadState is Cached).
        val cachedPath = localSource.getCachedEbookPath(bookUuid, bookType)

        return if (cachedPath != null) {
            Ok(cachedPath)
        } else {
            com.github.michaelbull.result.Err(
                AppError.UnknownError(
                    IllegalStateException(
                        "Ebook not cached. Use BookDownloadManager to download first."
                    )
                )
            )
        }
    }

    override suspend fun getReadingProgress(
        bookUuid: String,
    ): AppResult<PositionDomainModel?> {
        return remoteWithCacheFallback(
            remoteSource = {
                remoteSource.getPosition(bookUuid).map { it?.toDomain(bookUuid) }
            },
            cacheSource = {
                localSource.getReadingProgress(bookUuid).map { it?.toDomain() }
            },
            saveToCache = { position ->
                localSource.saveReadingProgress(position.toLocal())
            },
        ).onFailure { error ->
            logError(error, "Failed to get reading progress: bookUuid=$bookUuid")
        }
    }

    override suspend fun getLocalReadingProgress(
        bookUuid: String,
    ): AppResult<PositionDomainModel?> {
        return localSource.getReadingProgress(bookUuid).map { it?.toDomain() }
            .onFailure { error ->
                logError(error, "Failed to get local reading progress: bookUuid=$bookUuid")
            }
    }

    override suspend fun getRemoteReadingProgress(
        bookUuid: String,
    ): AppResult<PositionDomainModel?> {
        return remoteSource.getPosition(bookUuid).map { it?.toDomain(bookUuid) }
            .onFailure { error ->
                logError(error, "Failed to get remote reading progress: bookUuid=$bookUuid")
            }
    }

    override suspend fun saveReadingProgress(
        progress: PositionDomainModel,
    ): CompletableResult {
        // Save to local cache first
        localSource.saveReadingProgress(progress.toLocal()).onFailure { error ->
            logError(
                error,
                "Failed to save reading progress locally: bookUuid=${progress.bookUuid}"
            )
        }

        // Then sync to remote
        return remoteSource.updatePosition(
            bookUuid = progress.bookUuid,
            position = progress.toApiModel(),
        ).onFailure { error ->
            logError(
                error,
                "Failed to sync reading progress to remote: bookUuid=${progress.bookUuid}"
            )
        }
    }

    override fun getReaderSettings(): Flow<ReaderSettingsDomainModel> {
        return localSource.getReaderSettings().map { it.toDomain() }
    }

    override suspend fun saveReaderSettings(
        settings: ReaderSettingsDomainModel,
    ): CompletableResult {
        return localSource.saveReaderSettings(settings.toLocal()).onFailure { error ->
            logError(error, "Failed to save reader settings")
        }
    }

    override suspend fun isEbookCached(bookUuid: String, bookType: BookType): Boolean {
        return localSource.isEbookCached(bookUuid, bookType)
    }

    override suspend fun deleteEbookCache(bookUuid: String, bookType: BookType): Boolean {
        return localSource.deleteEbookCache(bookUuid, bookType)
    }

    private fun logError(error: AppError, message: String) {
        val throwable = when (error) {
            is AppError.NetworkError -> error.throwable
            is AppError.DatabaseError -> error.throwable
            is AppError.UnknownError -> error.throwable
            is AppError.ApiError -> Exception("API Error ${error.code}: ${error.message}")
        }
        analytics.logException(throwable, message)
    }
}

