package com.retro99.reader.ui.playback

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.di.ReaderScope
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/**
 * Controls the foreground service lifecycle for audio playback.
 *
 * This class is responsible for:
 * - Starting the foreground service when playback begins
 * - Stopping the foreground service when playback ends
 *
 * @param context Android context for starting/stopping the service
 * @param analytics Analytics for logging exceptions
 */
@Scope(ReaderScope::class)
@Scoped
class ForegroundServiceController(
    private val context: Context,
    @Provided private val analytics: Analytics,
) {
    /**
     * Starts the foreground service for background playback.
     * Should be called after notification permission is granted and audio focus is acquired.
     *
     * @return true if service was started successfully, false if blocked by system
     *         (e.g., app backgrounded during permission dialog on Android 12+)
     */
    fun startService(): Boolean {
        val intent = Intent(context, MediaPlaybackService::class.java)
        return try {
            ContextCompat.startForegroundService(context, intent)
            true
        } catch (e: Exception) {
            // On Android 12+, ForegroundServiceStartNotAllowedException is thrown
            // if the app isn't in a foreground-allowed state
            analytics.logException(e, "Failed to start foreground service")
            false
        }
    }

    /**
     * Stops the foreground service.
     * Should be called when playback ends or is stopped.
     */
    fun stopService() {
        val intent = Intent(context, MediaPlaybackService::class.java)
        context.stopService(intent)
    }
}

