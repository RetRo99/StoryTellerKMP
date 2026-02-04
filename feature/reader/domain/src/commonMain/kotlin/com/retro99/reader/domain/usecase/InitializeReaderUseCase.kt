package com.retro99.reader.domain.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.getOrElse
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import com.retro99.reader.domain.model.ReaderInitializationData
import com.retro99.reader.domain.model.ReadingProgressResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Use case that orchestrates the initialization of the reader.
 *
 * This use case handles:
 * 1. Fetching the book metadata to get the ebook file path
 * 2. Preparing/downloading the ebook file locally
 * 3. Loading initial reader settings
 * 4. Fetching reading progress with conflict detection
 *
 * This consolidates logic that was previously spread across the ViewModel,
 * making it more testable and keeping the ViewModel focused on UI state.
 */
@Factory
class InitializeReaderUseCase(
    @Provided private val getBookByUuidUseCase: GetBookByUuidUseCase,
    @Provided private val prepareEbookUseCase: PrepareEbookUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val getReadingProgressWithConflictUseCase: GetReadingProgressWithConflictUseCase,
) {
    /**
     * Initializes the reader for the given book.
     *
     * @param bookUuid The UUID of the book to open in the reader
     * @return [ReaderInitializationData] containing everything needed to open the publication,
     *         or an error if initialization fails
     */
    suspend operator fun invoke(bookUuid: String): AppResult<ReaderInitializationData> =
        coroutineScope {
            // First, get the book to find the ebook file path
            val bookResult = getBookByUuidUseCase(bookUuid).first()

            bookResult.flatMap { book ->
                val ebookFilePath = book.ebook?.filepath
                    ?: return@coroutineScope Err(
                        AppError.UnknownError(Throwable("Book has no ebook"))
                    )

                // Prepare the ebook (download if needed)
                prepareEbookUseCase(bookUuid, ebookFilePath).flatMap { localPath ->
                    // Load settings and progress in parallel
                    val settingsDeferred = async {
                        getReaderSettingsUseCase().first()
                    }
                    val progressDeferred = async {
                        getReadingProgressWithConflictUseCase(bookUuid)
                    }

                    val settings = settingsDeferred.await()
                    val progressResult = progressDeferred.await()
                        .getOrElse { ReadingProgressResult.Resolved(null) }

                    Ok(
                        ReaderInitializationData(
                            bookUuid = bookUuid,
                            localEbookPath = localPath,
                            initialSettings = settings,
                            progressResult = progressResult,
                        )
                    )
                }
            }
        }
}
