package com.retro99.reader.ui.bridge

import com.retro99.reader.ui.model.ReaderSettingsUiModel
import platform.UIKit.UIViewController

/**
 * Locator data class for iOS bridge.
 * Represents a position in the EPUB for restoration.
 */
data class EpubLocator(
    val href: String,
    val type: String,
    val title: String?,
    val progression: Double?,
    val position: Int?,
    val totalProgression: Double?,
)

/**
 * Reader settings data class for iOS bridge.
 * This is a simple data holder that can be passed to Swift.
 * Add new settings properties here as needed.
 */
data class EpubReaderSettings(
    val fontSize: Double,
    val fontFamily: String,
    val lineHeight: Float,
    val marginHorizontal: Int,
    val marginVertical: Int,
    val scrollMode: Boolean,
    val initialLocator: EpubLocator?,
) {
    companion object {
        fun from(
            settings: ReaderSettingsUiModel,
            initialLocator: EpubLocator? = null,
        ): EpubReaderSettings {
            return EpubReaderSettings(
                fontSize = settings.fontSize,
                fontFamily = settings.fontFamily,
                lineHeight = settings.lineHeight,
                marginHorizontal = settings.marginHorizontal,
                marginVertical = settings.marginVertical,
                scrollMode = settings.scrollMode,
                initialLocator = initialLocator,
            )
        }
    }
}

/**
 * Bridge interface for iOS EPUB reader functionality.
 * This interface is implemented in Swift and registered at app startup.
 * The Kotlin code uses this bridge to delegate to Readium Swift.
 */
interface EpubReaderBridge {
    /**
     * Opens an EPUB publication from the given file path.
     * @param filePath The absolute path to the EPUB file
     * @param onSuccess Called when the publication is successfully opened
     * @param onError Called with an error message if opening fails
     */
    fun openPublication(
        filePath: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    /**
     * Closes the currently open publication and releases resources.
     */
    fun closePublication()

    /**
     * Creates a UIViewController for displaying the EPUB content.
     * @param settings The initial reader settings to apply
     * @return A UIViewController that renders the EPUB, or null if no publication is open
     */
    fun createReaderViewController(settings: EpubReaderSettings): UIViewController?

    /**
     * Navigates to the next page/resource in the publication.
     */
    fun goToNextPage()

    /**
     * Navigates to the previous page/resource in the publication.
     */
    fun goToPreviousPage()

    /**
     * Navigates to a specific chapter by its href.
     * @param href The href of the chapter to navigate to
     */
    fun goToChapter(href: String)

    /**
     * Applies reader settings to the publication.
     * @param settings The reader settings to apply
     */
    fun setSettings(settings: EpubReaderSettings)
}

/**
 * Registry for the EPUB reader bridge.
 * Swift code registers its implementation here at app startup.
 */
object EpubReaderBridgeRegistry {
    private var bridge: EpubReaderBridge? = null

    /**
     * Registers the Swift implementation of the EPUB reader bridge.
     * Should be called from Swift during app initialization.
     */
    fun register(bridge: EpubReaderBridge) {
        this.bridge = bridge
    }

    /**
     * Gets the registered bridge implementation.
     * @return The bridge, or null if not registered
     */
    fun getBridge(): EpubReaderBridge? = bridge

    /**
     * Checks if a bridge has been registered.
     */
    fun isRegistered(): Boolean = bridge != null
}

