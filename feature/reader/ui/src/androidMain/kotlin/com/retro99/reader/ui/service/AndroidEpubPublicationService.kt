package com.retro99.reader.ui.service

import android.content.Context
import com.github.michaelbull.result.Ok
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.AppResult
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.publication.EpubPublication
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import java.io.File

/**
 * Android implementation of [EpubPublicationService] using Readium.
 *
 * This service manages the lifecycle of Readium's [Publication] object.
 * It opens EPUB files using Readium's streamer and provides access to the
 * publication for rendering in EpubNavigatorFragment.
 *
 * The service is scoped per reader session and should be cleaned up
 * when the reader screen is disposed.
 */
@Single(binds = [EpubPublicationService::class])
class AndroidEpubPublicationService(
    private val context: Context,
    analytics: Analytics,
) : BaseEpubPublicationService(analytics) {

    private val httpClient by lazy { DefaultHttpClient() }
    private val assetRetriever by lazy { AssetRetriever(context.contentResolver, httpClient) }
    private val publicationParser by lazy {
        DefaultPublicationParser(context, httpClient, assetRetriever, pdfFactory = null)
    }
    private val publicationOpener by lazy { PublicationOpener(publicationParser) }

    override suspend fun openPublication(
        filePath: String,
        bookUuid: String,
        initialSettings: ReaderSettingsUiModel,
        bookType: BookType,
        initialPosition: PositionUiModel?,
    ): AppResult<EpubPublication> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext createError("Ebook file not found: $filePath")
                }

                val url = file.toUrl()
                val asset = assetRetriever.retrieve(url).getOrElse { error ->
                    // Don't log file path for privacy - only log book UUID
                    analytics.logException(
                        Exception("Asset retrieval failed: ${error.message}"),
                        "AndroidEpubPublicationService: Failed to retrieve asset for book=$bookUuid",
                    )
                    return@withContext createError("Failed to retrieve ebook asset: ${error.message}")
                }

                val openedPublication = publicationOpener.open(asset, allowUserInteraction = false)
                    .getOrElse { error ->
                        // Don't log file path for privacy - only log book UUID
                        analytics.logException(
                            Exception("Publication open failed: ${error.message}"),
                            "AndroidEpubPublicationService: Failed to open publication for book=$bookUuid",
                        )
                        return@withContext createError("Failed to open ebook: ${error.message}")
                    }

                Ok(
                    EpubPublication(
                        openedPublication,
                        bookUuid,
                        initialSettings,
                        bookType,
                        initialPosition,
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                createError("Error opening ebook: ${e.message}")
            }
        }
}

