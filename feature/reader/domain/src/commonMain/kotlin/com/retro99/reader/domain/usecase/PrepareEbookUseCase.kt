package com.retro99.reader.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.BookType
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class PrepareEbookUseCase(
    @Provided private val readerRepository: ReaderRepository,
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
        return readerRepository.prepareEbook(bookUuid, ebookFilePath, bookType)
    }
}

