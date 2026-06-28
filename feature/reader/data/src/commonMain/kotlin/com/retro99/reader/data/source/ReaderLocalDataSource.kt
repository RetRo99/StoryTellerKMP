package com.retro99.reader.data.source

import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.BookmarksDatabase
import com.retro99.database.api.books.PositionDatabase
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.preferences.api.putObject
import com.retro99.preferences.implementation.usecase.GetUserPreferenceUseCase
import com.retro99.preferences.implementation.usecase.RemoveUserPreferenceUseCase
import com.retro99.preferences.implementation.usecase.SaveUserPreferenceUseCase
import com.retro99.books.domain.model.BookType
import com.retro99.reader.data.model.CustomReaderFontLocalModel
import com.retro99.reader.data.model.BookmarkLocalModel
import com.retro99.reader.data.model.CurrentlyReadingLocalModel
import com.retro99.reader.data.model.PositionLocalModel
import com.retro99.reader.data.model.ReaderSettingsLocalModel
import com.retro99.reader.data.model.toDomain
import com.retro99.reader.data.model.toLocal
import com.retro99.reader.data.model.toLocalModel
import com.retro99.reader.domain.model.CurrentlyReadingDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
    @Provided private val positionDatabase: PositionDatabase,
    @Provided private val bookmarksDatabase: BookmarksDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
    @Provided private val getUserPreferenceUseCase: GetUserPreferenceUseCase,
    @Provided private val saveUserPreferenceUseCase: SaveUserPreferenceUseCase,
    @Provided private val removeUserPreferenceUseCase: RemoveUserPreferenceUseCase,
) : ReaderLocalSource {

    private val _readerSettings = MutableStateFlow(loadReaderSettings())
    private val _customFonts = MutableStateFlow(loadCustomFonts())

    override suspend fun getReadingProgress(
        bookUuid: String,
    ): AppResult<PositionLocalModel?> {
        return databaseExecutor.executeDatabaseOperation {
            positionDatabase.getPositionByBookUuid(bookUuid)?.toLocalModel()
        }
    }

    override suspend fun saveReadingProgress(
        progress: PositionLocalModel,
    ): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            positionDatabase.upsertPosition(progress)
        }
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

    override fun getCustomFonts(): Flow<List<CustomReaderFontLocalModel>> {
        return _customFonts.asStateFlow()
    }

    override suspend fun saveCustomFonts(fonts: List<CustomReaderFontLocalModel>): CompletableResult {
        preferences.putObject(PreferencesKey.ReaderCustomFonts, fonts)
        _customFonts.value = fonts
        return Ok(Unit)
    }

    override suspend fun isEbookCached(bookUuid: String, bookType: BookType): Boolean {
        return fileDownloader.isEbookCached(bookUuid, bookType)
    }

    override suspend fun getCachedEbookPath(bookUuid: String, bookType: BookType): String? {
        return fileDownloader.getCachedEbookPath(bookUuid, bookType)
    }

    override suspend fun deleteEbookCache(bookUuid: String, bookType: BookType): Boolean {
        return fileDownloader.deleteEbookCache(bookUuid, bookType)
    }

    override fun getCurrentlyReading(): CurrentlyReadingDomainModel? {
        return getUserPreferenceUseCase<CurrentlyReadingLocalModel>(PreferencesKey.CurrentlyReading)?.toDomain()
    }

    override fun setCurrentlyReading(currentlyReading: CurrentlyReadingDomainModel) {
        saveUserPreferenceUseCase(PreferencesKey.CurrentlyReading, currentlyReading.toLocal())
    }

    override fun clearCurrentlyReading() {
        removeUserPreferenceUseCase(PreferencesKey.CurrentlyReading)
    }

    override suspend fun getAllPositions(): AppResult<List<PositionLocalModel>> {
        return databaseExecutor.executeDatabaseOperation {
            positionDatabase.getAllPositions().map { it.toLocalModel() }
        }
    }

    override fun observeBookmarks(bookUuid: String): Flow<List<BookmarkLocalModel>> {
        return bookmarksDatabase.observeBookmarks(bookUuid)
            .map { bookmarks -> bookmarks.map { it.toLocalModel() } }
    }

    override suspend fun addBookmark(bookmark: BookmarkLocalModel): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            bookmarksDatabase.addBookmark(bookmark)
        }
    }

    override suspend fun deleteBookmark(id: String): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            bookmarksDatabase.deleteBookmark(id)
        }
    }

    private fun loadReaderSettings(): ReaderSettingsLocalModel {
        val rawJson = preferences.getStringOrNull(PreferencesKey.ReaderSettings)

        val settings = preferences.getObject<ReaderSettingsLocalModel>(PreferencesKey.ReaderSettings)
            ?: ReaderSettingsLocalModel()

        return settings
    }

    private fun loadCustomFonts(): List<CustomReaderFontLocalModel> {
        return preferences.getObject<List<CustomReaderFontLocalModel>>(PreferencesKey.ReaderCustomFonts)
            ?: emptyList()
    }
}

