package com.retro99.reader.ui.playback

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import org.koin.core.annotation.Single

/**
 * Holds player and session together as an atomic unit.
 * This ensures both are always read/written together, preventing race conditions.
 */
data class PlayerSessionPair(
    val player: ExoPlayer,
    val session: MediaSession,
)

/**
 * Koin-managed controller for media playback state.
 *
 * This class acts as a bridge between [MediaPlaybackService] (created by Android)
 * and other components like [MediaSessionManager] and [MediaOverlayPlayer].
 *
 * It replaces the static singleton pattern previously used in [MediaPlaybackService]'s
 * companion object, enabling proper dependency injection and testability.
 *
 * ## Thread Safety
 *
 * All public methods use synchronized blocks to ensure that compound operations
 * (read + write + side effect) are atomic. This prevents race conditions where
 * registerPlayer() and onServiceCreated() could both call addSession().
 *
 * Note: We use synchronized instead of @MainThread because onServiceCreated() is
 * called by the Android system and we cannot guarantee the thread. The synchronized
 * blocks provide actual runtime thread safety rather than lint-only annotations.
 */
@Single
class MediaPlaybackController {

    private val lock = Any()

    // Guarded by lock
    private var _playerSession: PlayerSessionPair? = null

    // Guarded by lock
    private var _serviceInstance: MediaPlaybackService? = null

    // Track which sessions have been added to the service to prevent duplicates
    // Guarded by lock
    private var sessionAddedToService: MediaSession? = null

    val currentPlayer: ExoPlayer?
        get() = synchronized(lock) { _playerSession?.player }

    val currentSession: MediaSession?
        get() = synchronized(lock) { _playerSession?.session }

    /**
     * Registers the ExoPlayer and MediaSession with the controller.
     * Called by MediaSessionManager when it creates the session.
     *
     * If the service is already running, the session will be added to it immediately.
     *
     * Note: If a previous player/session pair exists, it will be silently replaced.
     * Callers should ensure the previous session is released before registering a new one.
     */
    fun registerPlayer(player: ExoPlayer, session: MediaSession) {
        synchronized(lock) {
            _playerSession = PlayerSessionPair(player, session)
            // Add the session to the service if it's already running and not already added
            val service = _serviceInstance
            if (service != null && sessionAddedToService != session) {
                service.addSession(session)
                sessionAddedToService = session
            }
        }
    }

    /**
     * Unregisters the player and session from the controller.
     * Called when playback is stopped or the player is released.
     */
    fun unregisterPlayer() {
        synchronized(lock) {
            _playerSession = null
            sessionAddedToService = null
        }
    }

    /**
     * Called by [MediaPlaybackService] when it's created.
     * If a session was registered before the service started, it will be added now.
     */
    fun onServiceCreated(service: MediaPlaybackService) {
        synchronized(lock) {
            _serviceInstance = service
            // If a session was registered before the service started, add it now
            val session = _playerSession?.session
            if (session != null && sessionAddedToService != session) {
                service.addSession(session)
                sessionAddedToService = session
            }
        }
    }

    /**
     * Called by [MediaPlaybackService] when it's destroyed.
     */
    fun onServiceDestroyed() {
        synchronized(lock) {
            _serviceInstance = null
            sessionAddedToService = null
        }
    }
}

