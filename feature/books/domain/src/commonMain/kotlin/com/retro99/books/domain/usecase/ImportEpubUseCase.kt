package com.retro99.books.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.books.domain.FileImportManager
import com.retro99.books.domain.model.BookDomainModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case for importing an EPUB file into the app.
 */
@Factory
class ImportEpubUseCase(
    @Provided private val fileImportManager: FileImportManager,
) {
    /**
     * Imports an EPUB file from the given bytes.
     *
     * @param fileBytes The raw bytes of the EPUB file
     * @param fileName The original file name
     * @return The imported book domain model, or an error if import fails
     */
    suspend operator fun invoke(
        fileBytes: ByteArray,
        fileName: String,
    ): AppResult<BookDomainModel.LocalBook> {
        return fileImportManager.importEpubFile(fileBytes, fileName)
    }
}

