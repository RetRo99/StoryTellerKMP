package com.retro99.reader.ui.navigator

import android.content.Context
import co.touchlab.kermit.Logger
import com.retro99.analytics.api.Analytics
import com.retro99.base.nowMillis
import com.retro99.reader.ui.audio.AudioController
import com.retro99.reader.ui.media.MediaOverlayPlayer
import com.retro99.reader.ui.media.smil.SmilParser
import com.retro99.reader.ui.media.smil.SmilQuickScanner
import com.retro99.reader.ui.model.AudioPositionState
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PlaybackState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.playback.MediaPlaybackController
import com.retro99.reader.ui.playback.NotificationPermissionHandler
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
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
 * @param smilParser Parser for SMIL media overlay files
 * @param quickScanner Quick scanner for SMIL file indexing
 */
@Single(
    binds = [
        BookController::class,
        AudioController::class,
    ]
)
class AndroidEpubNavigatorController internal constructor(
    private val context: Context,
    private val analytics: Analytics,
    private val smilParser: SmilParser,
    private val quickScanner: SmilQuickScanner,
    private val mediaPlaybackController: MediaPlaybackController,
    private val notificationPermissionHandler: NotificationPermissionHandler,
) : EpubNavigatorController {

    /**
     * Internal coroutine scope for this controller.
     * Uses SupervisorJob so that failure of one child doesn't cancel siblings.
     * Uses Dispatchers.Main.immediate for UI-related operations.
     */

    private val _navigator = MutableStateFlow<EpubNavigatorFragment?>(null)

    private var publication: EpubPublication? = null

    private val navigator: EpubNavigatorFragment
        get() = _navigator.value ?: error("Navigator not initialized")

    fun init(
        publication: EpubPublication,
        navigator: EpubNavigatorFragment,
    ) {
        this.publication = publication
        _navigator.value = navigator
        initializeMediaOverlays()
    }
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Flow of current reading position/locator changes.
     * Converts Readium's Locator to the common LocatorState model.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentLocator: Flow<LocatorState> = _navigator.flatMapLatest { navigator ->
        navigator?.currentLocator?.map { locator ->
            LocatorState(
                href = locator.href.toString(),
                type = locator.mediaType.toString(),
                title = locator.title,
                progression = locator.locations.progression,
                position = locator.locations.position,
                totalProgression = locator.locations.totalProgression,
            )
        } ?: flowOf() // or emptyFlow()
    }

    // Media overlay player for ReadAloud books - wrapped in StateFlow for reactive access
    private val _mediaOverlayPlayer = MutableStateFlow<MediaOverlayPlayer?>(null)

    /**
     * Sealed class representing pending media commands that should be executed
     * when the player becomes available.
     *
     * This prevents commands from being silently dropped when the user taps play
     * before SMIL initialization completes.
     */
    private sealed class PendingMediaCommand {
        data class Play(val initialPositionMs: Long?) : PendingMediaCommand()
        data object Resume : PendingMediaCommand()
        data object Pause : PendingMediaCommand()
        data class SeekTo(val timestampMs: Long) : PendingMediaCommand()
        data class SetSpeed(val speed: Float) : PendingMediaCommand()
    }

    /**
     * Queue of pending media commands to execute when the player becomes available.
     * Only the most recent command of each type is kept (e.g., multiple seeks are collapsed).
     */
    private var pendingCommand: PendingMediaCommand? = null

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
     *
     * Note: We use distinctUntilChanged() instead of drop(1) to avoid duplicate emissions
     * while still receiving the initial state. The previous drop(1) was dangerous because:
     * - It could swallow important state corrections (e.g., after permission denial)
     * - Every time _mediaOverlayPlayer emits a new player, drop(1) would skip the first
     *   value again, potentially missing critical state updates
     * - After config change/recreation, the initial false state would be dropped, leaving
     *   the ViewModel with stale optimistic state
     */
    override val isPlayingState: Flow<Boolean> = _mediaOverlayPlayer.flatMapLatest { player ->
        player?.isPlaying ?: flowOf()
    }

    /**
     * Flow of playback state changes (PLAYING, PAUSED, BUFFERING, STOPPED, ERROR).
     * Use this to show error feedback to the user when SMIL parsing fails or other errors occur.
     */
    override val playbackState: Flow<PlaybackState> = _mediaOverlayPlayer.flatMapLatest { player ->
        player?.playbackState ?: flowOf(PlaybackState.STOPPED)
    }

    /**
     * Flow that emits true when the media player is ready.
     */
    override val isPlayerReady: Flow<Boolean> = _mediaOverlayPlayer.map { it != null }

    /**
     * Flow that emits true when the notification permission was denied.
     */
    override val showPermissionDeniedDialog: Flow<Boolean> =
        _mediaOverlayPlayer.flatMapLatest { player ->
            player?.showPermissionDeniedDialog ?: flowOf(false)
        }

    /**
     * Flow that emits true when the permission denial dialog should show a rationale
     * (user can be asked again) vs directing to settings (permanently denied).
     */
    override val showPermissionRationale: Flow<Boolean> =
        _mediaOverlayPlayer.flatMapLatest { player ->
            player?.showPermissionRationale ?: flowOf(false)
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
        _navigator.value?.submitPreferences(settings.toEpubPreferences())
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
        if (publication == null || publication?.hasMediaOverlays != true) {
            return
        }

        controllerScope.launch {
            val totalStartTime = nowMillis()
            logger.i { "⏱️ MediaOverlay initialization STARTED (lazy loading)" }

            val playerCreateStart = nowMillis()
            val player = MediaOverlayPlayer(
                context = context,
                publication = publication!!.publication,
                analytics = analytics,
                smilParser = smilParser,
                quickScanner = quickScanner,
                mediaPlaybackController = mediaPlaybackController,
                notificationPermissionHandler = notificationPermissionHandler,
                onLocatorChanged = { locator ->
                    controllerScope.launch { applyHighlight(locator) }
                },
            )
            val playerCreateTime = nowMillis() - playerCreateStart
            logger.i { "⏱️ MediaOverlayPlayer created in ${playerCreateTime}ms" }

            // Get initial chapter href for optimized index building
            val initialChapterHref = navigator.currentLocator.value.href.toString()

            val initializeStart = nowMillis()
            player.initialize(initialChapterHref)
            val initializeTime = nowMillis() - initializeStart
            logger.i { "⏱️ player.initialize() (index building) completed in ${initializeTime}ms" }

            _mediaOverlayPlayer.value = player

            // Execute any pending command that was queued before the player was ready
            executePendingCommand(player)

            // Prepare duration for the initial chapter (this now parses SMIL on-demand)
            val chapterPrepareStart = nowMillis()
            player.prepareChapterDuration(navigator.currentLocator.value.href)
            val chapterPrepareTime = nowMillis() - chapterPrepareStart
            logger.i { "⏱️ prepareChapterDuration() completed in ${chapterPrepareTime}ms" }

            val totalTime = nowMillis() - totalStartTime
            logger.i { "⏱️ MediaOverlay initialization COMPLETE - TOTAL: ${totalTime}ms" }

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
        logger.i { "🎵 playAudio() called - initialPositionMs=$initialPositionMs" }
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing Play command" }
            pendingCommand = PendingMediaCommand.Play(initialPositionMs)
            return
        }
        executePlayCommand(player, initialPositionMs)
    }

    override fun resumeAudio() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing Resume command" }
            pendingCommand = PendingMediaCommand.Resume
            return
        }
        player.resume()
    }

    override fun pauseAudio() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            // For pause, we can clear any pending play/resume command
            // since the user changed their mind before playback started
            logger.i { "🎵 Player not ready, clearing pending command (pause requested)" }
            pendingCommand = null
            return
        }
        player.pause()
    }

    override fun seekToAudioPosition(timestampMs: Long) {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing SeekTo command: $timestampMs" }
            pendingCommand = PendingMediaCommand.SeekTo(timestampMs)
            return
        }
        player.seekTo(timestampMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, queuing SetSpeed command: $speed" }
            pendingCommand = PendingMediaCommand.SetSpeed(speed)
            return
        }
        player.setPlaybackSpeed(speed)
    }

    override fun skipForward() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, ignoring skipForward" }
            return
        }
        player.seekForward()
    }

    override fun skipBackward() {
        val player = _mediaOverlayPlayer.value
        if (player == null) {
            logger.i { "🎵 Player not ready, ignoring skipBackward" }
            return
        }
        player.seekBackward()
    }

    override fun dismissPermissionDeniedDialog() {
        _mediaOverlayPlayer.value?.dismissPermissionDeniedDialog()
    }

    /**
     * Executes the play command on the player.
     * Extracted to avoid code duplication between immediate and deferred execution.
     */
    private fun executePlayCommand(player: MediaOverlayPlayer, initialPositionMs: Long?) {
        logger.i { "🎵 MediaOverlayPlayer is available" }
        // Get the current locator with chapter href, fragment, and progression
        val currentLocator = navigator.currentLocator.value
        val currentChapterHref = currentLocator.href
        // Extract the fragment ID from the locator (e.g., "chapter44.xhtml-sentence50")
        val fragmentId = currentLocator.locations.fragments.firstOrNull()
        // Extract the progression (0.0 to 1.0) through the chapter
        val progression = currentLocator.locations.progression
        logger.i {
            "🎵 Calling player.play() - href=$currentChapterHref, " +
                    "fragmentId=$fragmentId, progression=$progression"
        }
        player.play(currentChapterHref, fragmentId, progression, initialPositionMs)
    }

    /**
     * Executes any pending media command that was queued before the player was ready.
     * Called after the player is initialized.
     */
    private fun executePendingCommand(player: MediaOverlayPlayer) {
        val command = pendingCommand ?: return
        pendingCommand = null

        logger.i { "🎵 Executing pending command: $command" }
        when (command) {
            is PendingMediaCommand.Play -> executePlayCommand(player, command.initialPositionMs)
            is PendingMediaCommand.Resume -> player.resume()
            is PendingMediaCommand.Pause -> player.pause()
            is PendingMediaCommand.SeekTo -> player.seekTo(command.timestampMs)
            is PendingMediaCommand.SetSpeed -> player.setPlaybackSpeed(command.speed)
        }
    }

    override fun close() {
        logger.i { "AndroidEpubNavigatorController RELEASE called" }
        val player = _mediaOverlayPlayer.value
        if (player != null) {
            logger.i { "Releasing MediaOverlayPlayer..." }
            player.release()
        } else {
            logger.w { "MediaOverlayPlayer was NULL - nothing to release" }
        }
        _mediaOverlayPlayer.value = null
        controllerScope.cancel()
        logger.i { "AndroidEpubNavigatorController RELEASED - scope cancelled" }
    }

    private companion object {
        private val logger = Logger.withTag("čič")
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
