package com.retro99.books.domain.usecase

import com.retro99.base.result.AppResult
import com.retro99.books.domain.FileImportManager
import com.retro99.books.domain.model.BookDomainModel
import io.github.vinceglb.filekit.core.PlatformFile
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
     * Imports an EPUB file from the given platform file.
     *
     * @param platformFile The platform file from the file picker
     * @return The imported book domain model, or an error if import fails
     */
    suspend operator fun invoke(
        platformFile: PlatformFile,
    ): AppResult<BookDomainModel.LocalBook> {
        return fileImportManager.importEpubFile(platformFile)
    }
}

