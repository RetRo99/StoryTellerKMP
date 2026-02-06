package com.retro99.reader.ui.bridge

import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import platform.UIKit.UIViewController

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
    val scrollMode: Boolean?,
    val initialPosition: PositionUiModel?,
) {
    companion object {
        fun from(
            settings: ReaderSettingsUiModel,
            initialPosition: PositionUiModel? = null,
        ): EpubReaderSettings {
            return EpubReaderSettings(
                fontSize = settings.fontSize,
                fontFamily = settings.fontFamily,
                lineHeight = settings.lineHeight,
                marginHorizontal = settings.marginHorizontal,
                marginVertical = settings.marginVertical,
                scrollMode = settings.scrollMode,
                initialPosition = initialPosition,
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
     * Navigates to a specific position in the publication.
     * @param href The href of the resource
     * @param type The media type of the resource
     * @param progression The progression within the resource (0.0 to 1.0)
     * @param position The position index, if available
     */
    fun goToPosition(
        href: String,
        type: String,
        progression: Double?,
        position: Int?,
    )

    /**
     * Applies reader settings to the publication.
     * @param settings The reader settings to apply
     */
    fun setSettings(settings: EpubReaderSettings)

    /**
     * Sets a callback to be invoked when the reading position changes.
     * @param callback The callback to invoke with position data, or null to clear
     */
    fun setOnPositionChangedCallback(callback: ((PositionLocator) -> Unit)?)

    // Media playback methods for ReadAloud books

    /**
     * Whether the publication has media overlays (audio narration).
     * @return true if the publication has SMIL files or audio resources
     */
    fun hasMediaOverlays(): Boolean

    /**
     * Initializes the media overlay player.
     * Should be called before attempting to play audio.
     * @param onReady Called when initialization is complete
     */
    fun initializeMediaOverlays(onReady: () -> Unit)

    /**
     * Starts audio playback for the current chapter, optionally seeking to a specific position.
     * @param initialPositionMs Optional initial position in milliseconds to seek to before playing
     */
    fun playAudio(initialPositionMs: Long? = null)

    /**
     * Resumes audio playback from the current position without seeking.
     */
    fun resumeAudio()

    /**
     * Pauses audio playback.
     */
    fun pauseAudio()

    /**
     * Seeks to a specific audio position.
     * @param timestampMs The position in milliseconds
     */
    fun seekToAudioPosition(timestampMs: Long)

    /**
     * Sets the playback speed.
     * @param speed The playback speed (e.g., 0.5, 1.0, 1.5, 2.0)
     */
    fun setPlaybackSpeed(speed: Float)

    /**
     * Sets a callback to be invoked when the audio playback state changes.
     * @param callback The callback with isPlaying state and current position in ms
     */
    fun setOnPlaybackStateChangedCallback(callback: ((PlaybackState) -> Unit)?)

    /**
     * Sets a callback to be invoked when the media player is ready.
     * @param callback The callback to invoke when the player is ready
     */
    fun setOnMediaPlayerReadyCallback(callback: (() -> Unit)?)
}

/**
 * Position locator data class for iOS bridge.
 * This is a simple data holder that can be passed from Swift to Kotlin.
 */
data class PositionLocator(
    val href: String,
    val type: String,
    val title: String?,
    val progression: Double?,
    val position: Int?,
    val totalProgression: Double?,
)

/**
 * Playback state data class for iOS bridge.
 * This is a simple data holder that can be passed from Swift to Kotlin.
 */
data class PlaybackState(
    val isPlaying: Boolean,
    val currentPositionMs: Long,
    val durationMs: Long?,
)

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

