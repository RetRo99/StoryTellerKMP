package com.retro99.reader.domain

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReadingProgressDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for reader-related operations.
 */
interface ReaderRepository {

    /**
     * Prepares an ebook for reading by downloading it if necessary.
     * Returns the local file path that can be used to open the publication.
     *
     * @param bookUuid The UUID of the book to prepare
     * @param ebookFilePath The file path of the ebook on the server
     * @return The local file path of the downloaded ebook
     */
    suspend fun prepareEbook(
        bookUuid: String,
        ebookFilePath: String,
    ): AppResult<String>

    /**
     * Gets the reading progress for a book.
     *
     * @param bookUuid The UUID of the book
     * @return The reading progress or null if not found
     */
    suspend fun getReadingProgress(bookUuid: String): AppResult<ReadingProgressDomainModel?>

    /**
     * Saves the reading progress for a book.
     *
     * @param progress The reading progress to save
     */
    suspend fun saveReadingProgress(progress: ReadingProgressDomainModel): CompletableResult

    /**
     * Gets the reader settings.
     *
     * @return Flow of reader settings
     */
    fun getReaderSettings(): Flow<ReaderSettingsDomainModel>

    /**
     * Saves the reader settings.
     *
     * @param settings The settings to save
     */
    suspend fun saveReaderSettings(settings: ReaderSettingsDomainModel): CompletableResult
}

