package com.retro99.reader.ui.bridge

import platform.UIKit.UIViewController

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
     * @param initialFontSize The initial font size scale to apply (1.0 = 100%)
     * @return A UIViewController that renders the EPUB, or null if no publication is open
     */
    fun createReaderViewController(initialFontSize: Double): UIViewController?

    /**
     * Navigates to the next page/resource in the publication.
     */
    fun goToNextPage()

    /**
     * Navigates to the previous page/resource in the publication.
     */
    fun goToPreviousPage()

    /**
     * Sets the font size scale for the publication.
     * @param scale The font size scale factor (1.0 = 100%, 2.0 = 200%, etc.)
     */
    fun setFontSize(scale: Double)
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

