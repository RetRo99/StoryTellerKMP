package com.retro99.reader.ui.controller

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.readium.r2.navigator.epub.EpubNavigatorFragment
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
) : EpubReaderController {

    private val httpClient by lazy { DefaultHttpClient() }
    private val assetRetriever by lazy { AssetRetriever(context.contentResolver, httpClient) }
    private val publicationParser by lazy {
        DefaultPublicationParser(context, httpClient, assetRetriever, pdfFactory = null)
    }
    private val publicationOpener by lazy { PublicationOpener(publicationParser) }

    private var publication: Publication? = null
    private var navigator: EpubNavigatorFragment? = null

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    override suspend fun openPublication(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _error.value = null
            _isReady.value = false

            val file = File(filePath)
            if (!file.exists()) {
                _error.value = "Ebook file not found: $filePath"
                return@withContext false
            }

            val url = file.toUrl()
            val asset = assetRetriever.retrieve(url).getOrNull()
            if (asset == null) {
                _error.value = "Failed to retrieve ebook asset"
                return@withContext false
            }

            val openedPublication = publicationOpener.open(asset, allowUserInteraction = false)
                .getOrNull()

            if (openedPublication == null) {
                _error.value = "Failed to open ebook"
                return@withContext false
            }

            publication = openedPublication
            _isReady.value = true
            true
        } catch (e: Exception) {
            _error.value = "Error opening ebook: ${e.message}"
            false
        }
    }

    override fun closePublication() {
        publication = null
        _isReady.value = false
        _error.value = null
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

    override fun goToNextPage() {
        navigator?.goForward()
    }

    override fun goToPreviousPage() {
        navigator?.goBackward()
    }
}

