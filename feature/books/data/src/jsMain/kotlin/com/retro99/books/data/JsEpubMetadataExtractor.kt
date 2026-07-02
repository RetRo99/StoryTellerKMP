package com.retro99.books.data

import com.github.michaelbull.result.Err
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import org.koin.core.annotation.Single

@Single(binds = [EpubMetadataExtractor::class])
class JsEpubMetadataExtractor : EpubMetadataExtractor {
    override suspend fun extractMetadata(filePath: String): AppResult<EpubMetadata> {
        return Err(AppError.NotFoundError("EPUB import not supported on web"))
    }
}
