package com.retro99.reader.ui.service

import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.publication.EpubPublication

/**
 * Service responsible for managing EPUB publication lifecycle.
 *
 * This service handles opening EPUB files. It is designed to be
 * injected into the ViewModel layer, separating publication management from
 * navigation concerns.
 *
 * Note: Reader settings and position are managed separately in [PublicationState],
 * which wraps the returned [EpubPublication] and allows settings/position to be
 * updated via `.copy()`.
 */
interface EpubPublicationService {

    /**
     * Opens an EPUB publication from the given file path.
     *
     * @param filePath The local file path to the EPUB file
     * @param serverId The ID of the server this book belongs to
     * @param bookUuid The unique identifier of the book
     * @param bookType The type of book (EBOOK, AUDIOBOOK, or READALOUD)
     * @return [AppResult] containing the opened [EpubPublication] on success, or [AppError] on failure
     */
    suspend fun openPublication(
        filePath: String,
        serverId: String,
        bookUuid: String,
        bookType: BookType = BookType.EBOOK,
    ): AppResult<EpubPublication>
}

