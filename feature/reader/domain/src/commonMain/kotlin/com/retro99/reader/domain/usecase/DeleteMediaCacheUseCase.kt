package com.retro99.reader.domain.usecase

import com.retro99.reader.domain.ReaderRepository
import com.retro99.reader.domain.model.BookType
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class DeleteMediaCacheUseCase(
    @Provided private val readerRepository: ReaderRepository,
) {
    /**
     * Deletes a cached media file.
     *
     * @param bookUuid The UUID of the book
     * @param bookType The type of media to delete (EBOOK, AUDIOBOOK, READALOUD)
     * @return True if the file was deleted successfully
     */
    suspend operator fun invoke(bookUuid: String, bookType: BookType): Boolean {
        return readerRepository.deleteEbookCache(bookUuid, bookType)
    }
}

