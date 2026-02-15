package com.retro99.reader.ui.service

import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.publication.EpubPublication

/**
 * Service responsible for managing EPUB publication lifecycle.
 *
 * This service handles opening EPUB files. It is designed to be
 * injected into the ViewModel layer, separating publication management from
 * navigation concerns.
 */
interface EpubPublicationService {

    /**
     * Opens an EPUB publication from the given file path.
     *
     * @param filePath The local file path to the EPUB file
     * @param bookUuid The unique identifier of the book
     * @param initialSettings The initial reader settings to apply when opening the publication
     * @param bookType The type of book (EBOOK, AUDIOBOOK, or READALOUD)
     * @param initialPosition The initial position to restore reading position, or null to start from beginning
     * @return [AppResult] containing the opened [EpubPublication] on success, or [AppError] on failure
     */
    suspend fun openPublication(
        filePath: String,
        bookUuid: String,
        initialSettings: ReaderSettingsUiModel,
        bookType: BookType = BookType.EBOOK,
        initialPosition: PositionUiModel? = null,
    ): AppResult<EpubPublication>
}

