package com.retro99.reader.ui.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.retro99.reader.ui.di.ReaderScope
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/**
 * Manages MediaSession integration for audio playback.
 *
 * This class is responsible ONLY for:
 * - Creating and managing the MediaSession
 * - Handling media button events (play/pause from headphones, car, etc.)
 * - Updating media metadata for lockscreen and notification display
 * - Registering/unregistering with the [MediaPlaybackController]
 *
 * Playback state tracking and foreground service management are handled
 * by [MediaOverlayPlayer] to avoid duplicate listeners and race conditions.
 */
@OptIn(UnstableApi::class)
@Scope(ReaderScope::class)
@Scoped
class MediaSessionManager(
    private val context: Context,
    private val player: ExoPlayer,
    private val controller: MediaPlaybackController,
    private val audioFocusManager: AudioFocusManager,
) {
    private var mediaSession: MediaSession? = null

    private var bookTitle: String = "Reading Aloud"
    private var chapterTitle: String? = null
    private var coverArtwork: ByteArray? = null

    /**
     * Tracks the last known playing state.
     * This is updated by a Player.Listener and used to reliably determine
     * if a PLAY_PAUSE command is a pause (was playing) or play (was paused).
     *
     * Using a tracked state is more reliable than checking player.isPlaying
     * in onPlayerCommandRequest, as it doesn't depend on timing assumptions
     * about when Media3 processes the command.
     *
     * Access is synchronized via [stateLock] to prevent TOCTOU races during
     * rapid play/pause sequences from the notification.
     */
    private var wasPlaying: Boolean = false

    /**
     * Lock for synchronizing access to [wasPlaying].
     * This prevents race conditions during rapid play/pause sequences where
     * onPlayerCommandRequest and onIsPlayingChanged might interleave.
     */
    private val stateLock = Any()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            synchronized(stateLock) {
                wasPlaying = isPlaying
            }
        }
    }

    /**
     * Initializes the MediaSession and registers it with the controller.
     */
    fun initialize() {
        if (mediaSession != null) return

        // Add listener to track playing state
        player.addListener(playerListener)

        // Create seek backward button (10 seconds)
        val seekBackwardButton = CommandButton.Builder(CommandButton.ICON_SKIP_BACK_10)
            .setDisplayName("Seek back 10 seconds")
            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
            .setSlots(CommandButton.SLOT_BACK)
            .build()

        // Create seek forward button (10 seconds)
        val seekForwardButton = CommandButton.Builder(CommandButton.ICON_SKIP_FORWARD_10)
            .setDisplayName("Seek forward 10 seconds")
            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
            .setSlots(CommandButton.SLOT_FORWARD)
            .build()

        // Create session activity PendingIntent to launch app when notification is tapped
        val sessionActivityIntent = createSessionActivityIntent()

        mediaSession = MediaSession.Builder(context, player)
            .setCallback(MediaSessionCallback())
            .setMediaButtonPreferences(ImmutableList.of(seekBackwardButton, seekForwardButton))
            .setSessionActivity(sessionActivityIntent)
            .build()

        // Register with the controller
        mediaSession?.let { session ->
            controller.registerPlayer(player, session)
        }
    }

    /**
     * Creates a PendingIntent that launches the app's main activity when the notification is tapped.
     * Uses the launcher intent to ensure proper task handling and back stack behavior.
     */
    private fun createSessionActivityIntent(): PendingIntent {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent().apply {
                // Fallback: create intent manually if launch intent is not available
                setPackage(context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        // Ensure proper flags for bringing existing task to front
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        return PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * Updates the stored metadata for notifications and lockscreen.
     * The metadata will be applied to new MediaItems when they are prepared.
     *
     * Note: This does NOT update currently playing media. For that, the caller
     * should rebuild the MediaItem with [buildCurrentMetadata] and set it on the player.
     *
     * @param bookTitle The title of the book
     * @param chapterTitle Optional chapter title
     * @param coverArtwork Optional cover image as PNG byte array for notification display
     */
    fun updateMetadata(
        bookTitle: String,
        chapterTitle: String? = null,
        coverArtwork: ByteArray? = null,
    ) {
        this.bookTitle = bookTitle
        this.chapterTitle = chapterTitle
        this.coverArtwork = coverArtwork
    }

    /**
     * Builds the current MediaMetadata based on stored book/chapter titles and cover.
     * Use this when creating a new MediaItem to ensure proper notification display.
     *
     * Media3's notification controller prefers MediaItem.mediaMetadata over
     * player.playlistMetadata, so metadata should be set on the MediaItem itself.
     */
    fun buildCurrentMetadata(): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(chapterTitle ?: bookTitle)
            .setArtist(if (chapterTitle != null) bookTitle else "StoryTeller")
            .setDisplayTitle(chapterTitle ?: bookTitle)
            .apply {
                coverArtwork?.let { artwork ->
                    setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                }
            }
            .build()
    }

    /**
     * Releases the MediaSession and unregisters from the controller.
     * Resets all tracked state to prevent stale values on reuse.
     */
    fun release() {
        player.removeListener(playerListener)
        controller.unregisterPlayer()
        mediaSession?.release()
        mediaSession = null
        // Reset tracked state to prevent stale values if this manager is reused
        synchronized(stateLock) {
            wasPlaying = false
        }
    }

    /**
     * Callback for handling MediaSession events.
     *
     * Intercepts pause commands from notification/lockscreen/Bluetooth to notify
     * [AudioFocusManager] that this is a user-initiated pause (not system focus loss).
     *
     * Also intercepts next/previous track commands from Bluetooth headsets and converts
     * them to 10-second seek forward/backward operations, since we don't have a playlist.
     */
    private inner class MediaSessionCallback : MediaSession.Callback {

        /**
         * Called when a controller connects to the session.
         *
         * We override this to explicitly add COMMAND_SEEK_TO_NEXT and COMMAND_SEEK_TO_PREVIOUS
         * to the available commands. Without this, these commands are not available when
         * there's only one item in the playlist (which is our case - single audio file per chapter).
         *
         * This enables Bluetooth headset next/previous buttons to trigger onPlayerCommandRequest.
         */
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            // Get the default available commands from the player
            val defaultCommands = super.onConnect(session, controller)

            // Build player commands including seek to next/previous
            // These are needed for Bluetooth headset buttons to work
            val playerCommands = Player.Commands.Builder()
                .addAll(defaultCommands.availablePlayerCommands)
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(defaultCommands.availableSessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int,
        ): Int {
            // Intercept pause command to notify AudioFocusManager
            // This prevents auto-resume when focus is regained after user paused from notification
            if (playerCommand == Player.COMMAND_PLAY_PAUSE ||
                playerCommand == Player.COMMAND_STOP
            ) {
                // Use tracked wasPlaying state instead of player.isPlaying.
                // This is more reliable as it doesn't depend on timing assumptions
                // about when Media3 processes the command internally.
                // Synchronized to prevent TOCTOU race with onIsPlayingChanged.
                val shouldNotifyPause = synchronized(stateLock) { wasPlaying }
                if (shouldNotifyPause) {
                    audioFocusManager.onUserPaused()
                }
            }

            // Intercept next/previous track commands and convert them to 10-second seek operations
            when (playerCommand) {
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    -> {
                    seekForward()
                    return SessionResult.RESULT_ERROR_NOT_SUPPORTED
                }

                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                    -> {
                    seekBackward()
                    return SessionResult.RESULT_ERROR_NOT_SUPPORTED
                }
            }

            return SessionResult.RESULT_SUCCESS
        }

        /**
         * Intercepts raw media button key events from Bluetooth headsets.
         *
         * Some Bluetooth headsets (like Sony WH-1000XM4) send KEYCODE_MEDIA_NEXT/PREVIOUS
         * which may not be properly translated to COMMAND_SEEK_TO_NEXT/PREVIOUS by the system.
         * By handling the raw key events here, we ensure consistent behavior.
         *
         * @return true if the event was handled, false to let the default handler process it
         */
        override fun onMediaButtonEvent(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            intent: Intent,
        ): Boolean {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

            // Only handle key down events to avoid double-triggering
            if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_NEXT,
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                        -> {
                        seekForward()
                        return true
                    }

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                    KeyEvent.KEYCODE_MEDIA_REWIND,
                        -> {
                        seekBackward()
                        return true
                    }
                }
            }

            // Let the default handler process other media button events
            return super.onMediaButtonEvent(session, controller, intent)
        }
    }

    /**
     * Seeks forward by 10 seconds from the current position.
     * The position is clamped to the duration of the current media.
     */
    private fun seekForward() {
        val newPosition = (player.currentPosition + SEEK_INCREMENT_MS)
            .coerceAtMost(player.duration.coerceAtLeast(0L))
        player.seekTo(newPosition)
    }

    /**
     * Seeks backward by 10 seconds from the current position.
     * The position is clamped to 0.
     */
    private fun seekBackward() {
        val newPosition = (player.currentPosition - SEEK_INCREMENT_MS)
            .coerceAtLeast(0L)
        player.seekTo(newPosition)
    }

    private companion object {
        /** Seek increment in milliseconds (10 seconds) */
        private const val SEEK_INCREMENT_MS = 10_000L
    }
}

