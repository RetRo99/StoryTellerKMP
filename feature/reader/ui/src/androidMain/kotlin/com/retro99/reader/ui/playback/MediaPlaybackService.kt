package com.retro99.reader.ui.playback

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import org.koin.android.ext.android.inject

/**
 * Foreground service for audio playback with MediaSession support.
 *
 * This service:
 * - Runs as a foreground service with a persistent notification
 * - Manages a MediaSession for system integration (lockscreen, Bluetooth, etc.)
 * - Handles audio focus and system audio policy
 * - Provides notification controls for play/pause/seek
 *
 * The service is started when audio playback begins and stopped when playback ends.
 * Media3's MediaSessionService automatically handles notification creation and updates.
 */
class MediaPlaybackService : MediaSessionService() {

    private val controller: MediaPlaybackController by inject()

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Timeout duration for stopping the service after task removal when paused.
     * 30 minutes is a reasonable balance between allowing resume and saving battery.
     */
    private val taskRemovedTimeoutMs = 30 * 60 * 1000L // 30 minutes

    private val stopSelfRunnable = Runnable {
        // Only stop if still paused - if user resumed, the listener will have cancelled this
        val player = controller.currentPlayer
        if (player == null || !player.isPlaying) {
            stopSelf()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                // User resumed playback - cancel any pending stop
                handler.removeCallbacks(stopSelfRunnable)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        controller.onServiceCreated(this)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return controller.currentSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep the service alive if there's any media loaded, even if paused.
        // This allows users to resume playback from the notification after
        // swiping the app from recents - standard behavior for audiobook/podcast apps.
        //
        // Only stop if there's truly nothing to play (no media items loaded).
        val player = controller.currentPlayer
        if (player == null || player.mediaItemCount == 0) {
            stopSelf()
            return
        }

        // If paused with content loaded, schedule a timeout to stop the service
        // This prevents indefinite battery drain if user forgets about the notification
        if (!player.isPlaying) {
            // Add listener to cancel timeout if user resumes
            player.addListener(playerListener)
            handler.postDelayed(stopSelfRunnable, taskRemovedTimeoutMs)
        }
    }

    override fun onDestroy() {
        // Clean up handler and listener
        handler.removeCallbacks(stopSelfRunnable)
        controller.currentPlayer?.removeListener(playerListener)
        controller.onServiceDestroyed()
        super.onDestroy()
    }
}

