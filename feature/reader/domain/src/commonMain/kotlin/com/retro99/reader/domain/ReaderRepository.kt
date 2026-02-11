package com.retro99.reader.domain

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.PositionDomainModel
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
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
     * @param bookType The type of book (determines the download format query)
     * @return The local file path of the downloaded ebook
     */
    suspend fun prepareEbook(
        bookUuid: String,
        ebookFilePath: String,
        bookType: BookType,
    ): AppResult<String>

    /**
     * Gets the reading progress for a book.
     * Uses remote with cache fallback strategy.
     *
     * @param bookUuid The UUID of the book
     * @return The reading progress or null if not found
     */
    suspend fun getReadingProgress(bookUuid: String): AppResult<PositionDomainModel?>

    /**
     * Gets the locally cached reading progress for a book.
     *
     * @param bookUuid The UUID of the book
     * @return The local reading progress or null if not found
     */
    suspend fun getLocalReadingProgress(bookUuid: String): AppResult<PositionDomainModel?>

    /**
     * Gets the remote reading progress for a book.
     *
     * @param bookUuid The UUID of the book
     * @return The remote reading progress or null if not found
     */
    suspend fun getRemoteReadingProgress(bookUuid: String): AppResult<PositionDomainModel?>

    /**
     * Saves the reading progress for a book.
     *
     * @param progress The reading progress to save
     */
    suspend fun saveReadingProgress(progress: PositionDomainModel): CompletableResult

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

    /**
     * Checks if an ebook file is cached locally.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of book (EBOOK, READALOUD, etc.)
     * @return True if the ebook file exists locally
     */
    suspend fun isEbookCached(bookUuid: String, bookType: BookType): Boolean
}

