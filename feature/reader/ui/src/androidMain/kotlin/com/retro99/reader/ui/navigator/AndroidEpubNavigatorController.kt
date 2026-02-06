package com.retro99.reader.ui.navigator

import android.content.Context
import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.media.MediaOverlayPlayer
import com.retro99.reader.ui.model.AudioPositionState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

/** Decoration group name for ReadAloud text highlighting */
private const val READALOUD_DECORATION_GROUP = "readaloud"

/** Highlight color for the currently spoken text (semi-transparent yellow) */
private const val READALOUD_HIGHLIGHT_COLOR = 0x80FFEB3B.toInt()

/**
 * Android implementation of [EpubNavigatorController] using Readium's EpubNavigatorFragment.
 *
 * This controller wraps the Readium navigator fragment and provides navigation
 * and settings functionality. It is created by the View after the publication
 * is ready and the navigator fragment is instantiated.
 *
 * For ReadAloud books with media overlays, this controller also manages audio playback
 * and text highlighting using the [MediaOverlayPlayer].
 *
 * @param navigator The Readium EpubNavigatorFragment to wrap
 * @param publication The Readium Publication for accessing metadata
 * @param context Android context for creating the media player
 * @param analytics Analytics instance for logging errors and events
 */
class AndroidEpubNavigatorController internal constructor(
    private val navigator: EpubNavigatorFragment,
    private val publication: EpubPublication,
    private val context: Context,
    private val analytics: Analytics,
) : EpubNavigatorController {

    /**
     * Internal coroutine scope for this controller.
     * Uses SupervisorJob so that failure of one child doesn't cancel siblings.
     * Uses Dispatchers.Main.immediate for UI-related operations.
     */
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Flow of current reading position/locator changes.
     * Converts Readium's Locator to the common LocatorState model.
     */
    override val currentLocator: Flow<LocatorState> = navigator.currentLocator.map { locator ->
        LocatorState(
            href = locator.href.toString(),
            type = locator.mediaType.toString(),
            title = locator.title,
            progression = locator.locations.progression,
            position = locator.locations.position,
            totalProgression = locator.locations.totalProgression,
        )
    }

    // Media overlay player for ReadAloud books - wrapped in StateFlow for reactive access
    private val _mediaOverlayPlayer = MutableStateFlow<MediaOverlayPlayer?>(null)

    /**
     * Flow of audio position updates (currentPosition and totalDuration).
     * Emits on every position change from the media player.
     */
    override val audioPositionState: Flow<AudioPositionState> =
        _mediaOverlayPlayer.flatMapLatest { player ->
            player?.let {
                combine(
                    it.currentPosition,
                    it.totalDuration,
                ) { positionMs, durationMs ->
                    AudioPositionState(
                        currentPositionMs = positionMs,
                        totalDurationMs = durationMs,
                    )
                }
            } ?: flowOf()
        }

    /**
     * Flow of playing state changes.
     * Drops the first emission to skip the initial state from player initialization.
     */
    override val isPlayingState: Flow<Boolean> = _mediaOverlayPlayer.flatMapLatest { player ->
        player?.isPlaying?.drop(1) ?: flowOf()
    }

    /**
     * Flow that emits true when the media player is ready.
     */
    override val isPlayerReady: Flow<Boolean> = _mediaOverlayPlayer.map { it != null }

    init {
        // Initialize media overlays automatically if the book supports them
        initializeMediaOverlays()
    }

    override fun goToNextPage() {
        navigator.goForward()
    }

    override fun goToPreviousPage() {
        navigator.goBackward()
    }

    override fun goToChapter(href: String) {
        val url = Url(href) ?: return
        val link = Link(href = url)
        navigator.go(link)
    }

    override fun setSettings(settings: ReaderSettingsUiModel) {
        navigator.submitPreferences(settings.toEpubPreferences())
    }

    override fun goToPosition(position: PositionUiModel) {
        val locator = position.toAndroidLocator() ?: return
        navigator.go(locator)
    }

    /**
     * Initializes the media overlay player for ReadAloud books.
     * Called automatically from the init block if the book has media overlays.
     * The initialization is launched in the controller's own scope.
     */
    private fun initializeMediaOverlays() {
        if (!publication.hasMediaOverlays) {
            return
        }

        controllerScope.launch {
            val player = MediaOverlayPlayer(
                context = context,
                publication = publication.publication,
                analytics = analytics,
                onLocatorChanged = { locator ->
                    controllerScope.launch { applyHighlight(locator) }
                },
            )
            player.initialize()
            _mediaOverlayPlayer.value = player

            // Prepare duration for the initial chapter
            val initialChapterHref = navigator.currentLocator.value.href
            player.prepareChapterDuration(initialChapterHref)

            navigator.currentLocator
                .map { it.href }
                .distinctUntilChanged()
                .collect { chapterHref ->
                    player.prepareChapterDuration(chapterHref)
                }
        }
    }

    /**
     * Applies a highlight decoration to the given locator and navigates to it.
     * This ensures the currently spoken text is always visible on screen.
     */
    private suspend fun applyHighlight(locator: Locator) {
        val decorableNavigator = navigator as? DecorableNavigator ?: return

        val decoration = Decoration(
            id = "readaloud-active",
            locator = locator,
            style = Decoration.Style.Highlight(
                tint = READALOUD_HIGHLIGHT_COLOR,
                isActive = true,
            ),
        )

        decorableNavigator.applyDecorations(listOf(decoration), READALOUD_DECORATION_GROUP)

        // Navigate to the locator to ensure the highlighted text is visible on screen
        // This is especially important when seeking audio - the text should follow
        navigator.go(locator)
    }

    override fun playAudio(initialPositionMs: Long?) {
        val player = _mediaOverlayPlayer.value ?: run {
            logPlayerNotInitialized("playAudio")
            return
        }

        // Get the current locator with chapter href, fragment, and progression
        val currentLocator = navigator.currentLocator.value
        val currentChapterHref = currentLocator.href
        // Extract the fragment ID from the locator (e.g., "chapter44.xhtml-sentence50")
        val fragmentId = currentLocator.locations.fragments.firstOrNull()
        // Extract the progression (0.0 to 1.0) through the chapter
        val progression = currentLocator.locations.progression
        player.play(currentChapterHref, fragmentId, progression, initialPositionMs)
    }

    override fun resumeAudio() {
        val player = _mediaOverlayPlayer.value ?: run {
            logPlayerNotInitialized("resumeAudio")
            return
        }
        player.resume()
    }

    override fun pauseAudio() {
        val player = _mediaOverlayPlayer.value ?: run {
            logPlayerNotInitialized("pauseAudio")
            return
        }
        player.pause()
    }

    override fun seekToAudioPosition(timestampMs: Long) {
        val player = _mediaOverlayPlayer.value ?: run {
            logPlayerNotInitialized("seekToAudioPosition")
            return
        }
        player.seekTo(timestampMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        val player = _mediaOverlayPlayer.value ?: run {
            logPlayerNotInitialized("setPlaybackSpeed")
            return
        }
        player.setPlaybackSpeed(speed)
    }

    /**
     * Logs an error when a media method is called before the player is initialized.
     */
    private fun logPlayerNotInitialized(methodName: String) {
        analytics.logException(
            throwable = IllegalStateException("Media player not initialized"),
            message = "$methodName called before media player was initialized",
        )
    }

    /**
     * Releases resources used by the media overlay player.
     * Should be called when the controller is no longer needed.
     */
    fun release() {
        _mediaOverlayPlayer.value?.release()
        _mediaOverlayPlayer.value = null
        controllerScope.cancel()
    }
}

/**
 * Extension function to convert ReaderSettingsUiModel to EpubPreferences.
 * This is used for initial preferences when creating the navigator.
 */
fun ReaderSettingsUiModel.toEpubPreferences(): EpubPreferences {
    return EpubPreferences(
        fontSize = fontSize,
        scroll = scrollMode,
        // Add more preferences here as needed:
        // fontFamily = fontFamily,
        // lineHeight = lineHeight,
        // etc.
    )
}

/**
 * Extension function to convert PositionUiModel to Readium Locator.
 * Returns null if the URL or mediaType cannot be parsed.
 */
fun PositionUiModel.toAndroidLocator(): Locator? {
    val url = Url(href) ?: return null
    val mediaType = MediaType(type) ?: return null

    return Locator(
        href = url,
        mediaType = mediaType,
        title = title,
        locations = Locator.Locations(
            progression = progression,
            position = position,
            totalProgression = totalProgression,
        ),
    )
}
