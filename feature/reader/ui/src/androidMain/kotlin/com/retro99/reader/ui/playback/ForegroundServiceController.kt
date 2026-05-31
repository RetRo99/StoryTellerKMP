package com.retro99.reader.ui.playback

import android.content.Context
import android.content.Intent
import android.util.Log
import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.di.ReaderScope
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

private const val TAG = "čič123"

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
    init {
        Log.d(TAG, "ForegroundServiceController CREATED (new instance)")
    }

    /**
     * Starts the foreground service for background playback.
     * Should be called after notification permission is granted and audio focus is acquired.
     *
     * @return true if service was started successfully, false if blocked by system
     *         (e.g., app backgrounded during permission dialog on Android 12+)
     */
    fun startService(): Boolean {
        Log.d(TAG, "ForegroundServiceController.startService() called", Exception("stack trace"))
        val intent = Intent(context, MediaPlaybackService::class.java)
        return try {
            context.startService(intent)
            Log.d(TAG, "ForegroundServiceController.startService() SUCCESS")
            true
        } catch (e: Exception) {
            // On Android 8+, background service starts are restricted. Playback is
            // initiated from the foreground reader UI, so this should normally pass.
            Log.e(TAG, "ForegroundServiceController.startService() FAILED: ${e.message}")
            analytics.logException(e, "Failed to start foreground service")
            false
        }
    }

    /**
     * Stops the foreground service.
     * Should be called when playback ends or is stopped.
     */
    fun stopService() {
        Log.d(TAG, "ForegroundServiceController.stopService() called", Exception("stack trace"))
        val intent = Intent(context, MediaPlaybackService::class.java)
        context.stopService(intent)
    }
}

