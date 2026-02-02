package com.retro99.reader.ui.service

import android.content.Context
import com.retro99.reader.domain.model.InitialLocatorDomainModel
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.AssetRetriever
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
class AndroidEpubPublicationService(
    private val context: Context,
) : BaseEpubPublicationService() {

    private val httpClient by lazy { DefaultHttpClient() }
    private val assetRetriever by lazy { AssetRetriever(context.contentResolver, httpClient) }
    private val publicationParser by lazy {
        DefaultPublicationParser(context, httpClient, assetRetriever, pdfFactory = null)
    }
    private val publicationOpener by lazy { PublicationOpener(publicationParser) }

    private var publication: Publication? = null
    private var initialSettings: ReaderSettingsDomainModel? = null
    private var initialLocator: InitialLocatorDomainModel? = null

    override suspend fun openPublication(
        filePath: String,
        initialSettings: ReaderSettingsDomainModel,
        initialLocator: InitialLocatorDomainModel?,
    ): EpubPublication? =
        withContext(Dispatchers.IO) {
            try {
                resetState()
                this@AndroidEpubPublicationService.initialSettings = initialSettings
                this@AndroidEpubPublicationService.initialLocator = initialLocator

                val file = File(filePath)
                if (!file.exists()) {
                    setError("Ebook file not found: $filePath")
                    return@withContext null
                }

                val url = file.toUrl()
                val asset = assetRetriever.retrieve(url).getOrNull()
                if (asset == null) {
                    setError("Failed to retrieve ebook asset")
                    return@withContext null
                }

                val openedPublication = publicationOpener.open(asset, allowUserInteraction = false)
                    .getOrNull()

                if (openedPublication == null) {
                    setError("Failed to open ebook")
                    return@withContext null
                }

                publication = openedPublication
                setReady()
                EpubPublication(openedPublication, initialSettings, initialLocator)
            } catch (e: Exception) {
                setError("Error opening ebook: ${e.message}")
                null
            }
        }

    override fun closePublication() {
        publication = null
        initialSettings = null
        resetState()
    }

    /**
     * Gets the currently open Readium Publication.
     * This is Android-specific and used by the View to create the navigator.
     *
     * @return The open Publication, or null if no publication is open
     */
    fun getPublication(): Publication? = publication

    /**
     * Gets the initial settings that were used to open the publication.
     * This is Android-specific and used by the View to create the navigator with initial preferences.
     *
     * @return The initial settings, or null if no publication is open
     */
    fun getInitialSettings(): ReaderSettingsDomainModel? = initialSettings
}

