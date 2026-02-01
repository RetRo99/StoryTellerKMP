package com.retro99.reader.ui.controller

import android.content.Context
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import java.io.File

/**
 * Android implementation of [EpubReaderController] using Readium.
 *
 * This controller manages the lifecycle of Readium's [Publication] object.
 * It opens EPUB files using Readium's streamer and provides access to the
 * publication for rendering in [EpubNavigatorFragment].
 *
 * The controller is scoped per reader session and should be cleaned up
 * when the reader screen is disposed.
 */
class AndroidEpubReaderController(
    private val context: Context,
) : BaseEpubReaderController() {

    private val httpClient by lazy { DefaultHttpClient() }
    private val assetRetriever by lazy { AssetRetriever(context.contentResolver, httpClient) }
    private val publicationParser by lazy {
        DefaultPublicationParser(context, httpClient, assetRetriever, pdfFactory = null)
    }
    private val publicationOpener by lazy { PublicationOpener(publicationParser) }

    private var publication: Publication? = null
    private var navigator: EpubNavigatorFragment? = null

    override suspend fun openPublication(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            resetState()

            val file = File(filePath)
            if (!file.exists()) {
                setError("Ebook file not found: $filePath")
                return@withContext false
            }

            val url = file.toUrl()
            val asset = assetRetriever.retrieve(url).getOrNull()
            if (asset == null) {
                setError("Failed to retrieve ebook asset")
                return@withContext false
            }

            val openedPublication = publicationOpener.open(asset, allowUserInteraction = false)
                .getOrNull()

            if (openedPublication == null) {
                setError("Failed to open ebook")
                return@withContext false
            }

            publication = openedPublication
            setReady()
            true
        } catch (e: Exception) {
            setError("Error opening ebook: ${e.message}")
            false
        }
    }

    override fun closePublication() {
        publication = null
        navigator = null
        resetState()
    }

    /**
     * Gets the currently open Readium Publication.
     * This is Android-specific and used by [EpubReaderView] to create the navigator.
     *
     * @return The open Publication, or null if no publication is open
     */
    fun getPublication(): Publication? = publication

    /**
     * Sets the navigator fragment for page navigation.
     * This is called by [EpubReaderView] after the fragment is created.
     */
    fun setNavigator(navigatorFragment: EpubNavigatorFragment) {
        navigator = navigatorFragment
    }

    /**
     * Clears the navigator reference.
     * This should be called when the reader view is disposed.
     */
    fun clearNavigator() {
        navigator = null
    }

    override fun goToNextPage() {
        navigator?.goForward()
    }

    override fun goToPreviousPage() {
        navigator?.goBackward()
    }

    override fun setSettings(settings: ReaderSettingsDomainModel) {
        navigator?.submitPreferences(settings.toEpubPreferences())
    }

    /**
     * Converts reader settings to Readium EpubPreferences.
     * This is used both for initial preferences and dynamic updates.
     * Add new preference mappings here as needed.
     */
    fun ReaderSettingsDomainModel.toEpubPreferences(): EpubPreferences {
        return EpubPreferences(
            fontSize = fontSize,
            // Add more preferences here as needed:
            // fontFamily = fontFamily,
            // lineHeight = lineHeight,
            // etc.
        )
    }
}

