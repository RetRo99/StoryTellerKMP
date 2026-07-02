package com.retro99.books.data

import com.github.michaelbull.result.Err
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.github.michaelbull.result.Ok
import com.retro99.books.domain.FileImportManager
import com.retro99.books.domain.model.BookDomainModel
import io.github.vinceglb.filekit.core.PlatformFile
import org.koin.core.annotation.Single

@Single(binds = [FileImportManager::class])
class JsFileImportManager : FileImportManager {
    override suspend fun importEpubFile(platformFile: PlatformFile): AppResult<BookDomainModel.LocalBook> {
        return Err(AppError.NotFoundError("File import not supported on web"))
    }

    override fun getImportedBookPath(uuid: String): String? = null
    override fun deleteImportedBookFiles(uuid: String): Boolean = false
    override suspend fun deleteLocalBook(uuid: String): CompletableResult = Ok(Unit)
}
