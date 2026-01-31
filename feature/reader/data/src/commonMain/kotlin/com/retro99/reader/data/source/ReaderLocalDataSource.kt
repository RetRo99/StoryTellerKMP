package com.retro99.reader.data.source

import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.preferences.api.putObject
import com.retro99.reader.data.model.ReaderSettingsLocalModel
import com.retro99.reader.data.model.ReadingProgressLocalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Implementation of [ReaderLocalSource].
 * Handles reading progress, settings, and file caching.
 *
 * Note: Publication opening/closing is handled by [EpubReaderController] in the UI layer.
 */
@Single(binds = [ReaderLocalSource::class])
class ReaderLocalDataSource(
    @Provided private val preferences: Preferences,
    @Provided private val fileDownloader: EbookFileDownloader,
) : ReaderLocalSource {

    private val _readerSettings = MutableStateFlow(loadReaderSettings())

    override suspend fun getReadingProgress(
        bookUuid: String,
    ): AppResult<ReadingProgressLocalModel?> {
        val localModel = preferences.getObject<ReadingProgressLocalModel>(
            PreferencesKey.ReadingProgress(bookUuid),
        )
        return Ok(localModel)
    }

    override suspend fun saveReadingProgress(
        progress: ReadingProgressLocalModel,
    ): CompletableResult {
        preferences.putObject(
            PreferencesKey.ReadingProgress(progress.bookUuid),
            progress,
        )
        return Ok(Unit)
    }

    override fun getReaderSettings(): Flow<ReaderSettingsLocalModel> {
        return _readerSettings.asStateFlow()
    }

    override suspend fun saveReaderSettings(
        settings: ReaderSettingsLocalModel,
    ): CompletableResult {
        preferences.putObject(PreferencesKey.ReaderSettings, settings)
        _readerSettings.value = settings
        return Ok(Unit)
    }

    override suspend fun isEbookCached(bookUuid: String): Boolean {
        return fileDownloader.isEbookCached(bookUuid)
    }

    override suspend fun getCachedEbookPath(bookUuid: String): String? {
        return fileDownloader.getCachedEbookPath(bookUuid)
    }

    private fun loadReaderSettings(): ReaderSettingsLocalModel {
        return preferences.getObject<ReaderSettingsLocalModel>(PreferencesKey.ReaderSettings)
            ?: ReaderSettingsLocalModel()
    }
}

