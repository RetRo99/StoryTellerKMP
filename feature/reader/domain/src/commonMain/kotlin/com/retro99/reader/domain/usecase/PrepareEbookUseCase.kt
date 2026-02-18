package com.retro99.reader.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.books.domain.model.BookType
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class PrepareEbookUseCase(
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
) {
    /**
     * Prepares the ebook file for reading.
     * Downloads the file if necessary and returns the local file path.
     *
     * @param bookUuid The unique identifier of the book
     * @param ebookFilePath The remote or local path of the ebook file
     * @param bookType The type of book (determines the download format query)
     * @return The local file path where the ebook is available
     */
    suspend operator fun invoke(
        bookUuid: String,
        ebookFilePath: String,
        bookType: BookType,
    ): AppResult<String> {
        return readerSettingsRepository.prepareEbook(bookUuid, ebookFilePath, bookType)
    }
}

