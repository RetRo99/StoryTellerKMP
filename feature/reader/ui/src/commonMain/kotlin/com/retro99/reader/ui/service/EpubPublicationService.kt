package com.retro99.reader.ui.service

import com.retro99.reader.domain.model.InitialLocatorDomainModel
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.flow.StateFlow

/**
 * Service responsible for managing EPUB publication lifecycle.
 *
 * This service handles opening and closing EPUB files. It is designed to be
 * injected into the ViewModel layer, separating publication management from
 * navigation concerns.
 */
interface EpubPublicationService {

    /**
     * Whether the publication is ready for reading.
     */
    val isReady: StateFlow<Boolean>

    /**
     * Error message if publication opening failed, null otherwise.
     */
    val error: StateFlow<String?>

    /**
     * Opens an EPUB publication from the given file path.
     *
     * @param filePath The local file path to the EPUB file
     * @param initialSettings The initial reader settings to apply when opening the publication
     * @param initialLocator The initial locator to restore reading position, or null to start from beginning
     * @return The opened [EpubPublication], or null if opening failed
     */
    suspend fun openPublication(
        filePath: String,
        initialSettings: ReaderSettingsDomainModel,
        initialLocator: InitialLocatorDomainModel? = null,
    ): EpubPublication?

    /**
     * Closes the currently open publication and releases resources.
     */
    fun closePublication()
}

