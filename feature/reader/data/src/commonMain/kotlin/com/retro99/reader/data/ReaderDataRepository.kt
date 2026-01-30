package com.retro99.reader.data

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.data.source.ReaderLocalSource
import com.retro99.reader.data.source.ReaderRemoteSource
import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReadingProgressDomainModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [ReaderRepository::class])
internal class ReaderDataRepository(
    @Provided private val localSource: ReaderLocalSource,
    @Provided private val remoteSource: ReaderRemoteSource,
    @Provided private val analytics: Analytics,
) : ReaderRepository {

    override suspend fun prepareEbook(
        bookUuid: String,
        ebookFilePath: String,
    ): AppResult<String> {
        // Check if ebook is already cached
        val cachedPath = localSource.getCachedEbookPath(bookUuid)

        return if (cachedPath != null) {
            Ok(cachedPath)
        } else {
            // Download the ebook
            remoteSource.downloadEbook(ebookFilePath, bookUuid)
        }.onFailure { error ->
            logError(error, "Failed to prepare ebook: bookUuid=$bookUuid")
        }
    }

    override suspend fun getReadingProgress(
        bookUuid: String,
    ): AppResult<ReadingProgressDomainModel?> {
        return localSource.getReadingProgress(bookUuid).onFailure { error ->
            logError(error, "Failed to get reading progress: bookUuid=$bookUuid")
        }
    }

    override suspend fun saveReadingProgress(
        progress: ReadingProgressDomainModel,
    ): CompletableResult {
        return localSource.saveReadingProgress(progress).onFailure { error ->
            logError(error, "Failed to save reading progress: bookUuid=${progress.bookUuid}")
        }
    }

    override fun getReaderSettings(): Flow<ReaderSettingsDomainModel> {
        return localSource.getReaderSettings()
    }

    override suspend fun saveReaderSettings(
        settings: ReaderSettingsDomainModel,
    ): CompletableResult {
        return localSource.saveReaderSettings(settings).onFailure { error ->
            logError(error, "Failed to save reader settings")
        }
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

