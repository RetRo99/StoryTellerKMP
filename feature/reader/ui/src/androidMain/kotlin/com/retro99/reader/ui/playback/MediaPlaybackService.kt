package com.retro99.reader.ui.playback

import android.content.Intent
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
        }
        // If paused with content loaded, keep service running for notification resume
    }

    override fun onDestroy() {
        controller.onServiceDestroyed()
        super.onDestroy()
    }
}

