package com.retro99.reader.ui.playback

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Single
import java.lang.ref.WeakReference

/**
 * Represents the result of a permission denial.
 * Used to show appropriate UI feedback to the user.
 */
sealed class PermissionDenialState {
    /**
     * Permission was not denied - either granted or not yet requested.
     */
    data object None : PermissionDenialState()

    /**
     * User denied permission but can be asked again.
     * Show rationale explaining why the permission is needed.
     */
    data object ShowRationale : PermissionDenialState()

    /**
     * User selected "Don't ask again" or denied multiple times.
     * Must direct user to app settings to enable permission.
     */
    data object PermanentlyDenied : PermissionDenialState()
}

/**
 * Handles notification permission requests for Android 13+ (TIRAMISU).
 *
 * This handler is registered in the Activity and can be called from anywhere
 * to request notification permission before starting the foreground service.
 *
 * ## Config Change Handling
 *
 * This handler survives configuration changes by:
 * 1. Keeping the permission result flow across Activity recreations
 * 2. Re-registering the launcher with the new Activity instance
 * 3. Using a request ID to correlate requests with results
 *
 * If a config change occurs during a permission request, the waiting coroutine
 * will poll for permission grant status rather than relying solely on the callback.
 *
 * Usage:
 * 1. Call [register] in Activity.onCreate() before setContent
 * 2. Call [ensurePermission] before starting the foreground service
 * 3. Call [unregister] in Activity.onDestroy() only if isFinishing is true
 */
@Single
class NotificationPermissionHandler {

    private var activityRef: WeakReference<ComponentActivity>? = null
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private val permissionResult = MutableStateFlow<Boolean?>(null)
    private val permissionMutex = Mutex()

    // Tracks if a permission request is currently in flight
    @Volatile
    private var requestInFlight = false

    // Exposes the current denial state for UI to observe
    private val _denialState = MutableStateFlow<PermissionDenialState>(PermissionDenialState.None)

    /**
     * Flow that emits the current permission denial state.
     * UI can observe this to show appropriate feedback (rationale vs settings).
     */
    val denialState: StateFlow<PermissionDenialState> = _denialState.asStateFlow()

    /**
     * Registers the permission launcher with the Activity.
     * Must be called in Activity.onCreate() before setContent.
     */
    fun register(activity: ComponentActivity) {
        this.activityRef = WeakReference(activity)
        this.permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            permissionResult.value = isGranted
            requestInFlight = false
        }
    }

    /**
     * Unregisters the handler when the Activity is destroyed.
     *
     * Note: Only call this when the Activity is truly finishing (isFinishing == true),
     * not during configuration changes. The launcher will be re-registered in onCreate.
     */
    fun unregister() {
        activityRef?.clear()
        activityRef = null
        permissionLauncher = null
        // Don't clear permissionResult - it needs to survive config changes
    }

    /**
     * Checks if notification permission is granted.
     * On Android < 13, always returns true.
     *
     * @return true if permission is granted, false if not granted or if activity is unavailable
     */
    fun hasPermission(): Boolean {
        // If activity is null, we can't check permission - assume not granted
        // This is safer than assuming granted, as it will trigger a permission request
        val currentActivity = activityRef?.get() ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                currentActivity,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks if we should show a rationale for the permission request.
     * Returns true if the user has previously denied the permission but hasn't
     * selected "Don't ask again".
     *
     * @return true if rationale should be shown, false otherwise
     */
    fun shouldShowRationale(): Boolean {
        val activity = activityRef?.get() ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            false
        }
    }

    /**
     * Checks if the user has permanently denied the permission ("Don't ask again").
     * This is inferred when:
     * - Permission is not granted
     * - shouldShowRationale returns false (user selected "Don't ask again")
     * - We've previously requested the permission (tracked by having a non-null result)
     *
     * @return true if user needs to go to settings, false otherwise
     */
    fun isPermanentlyDenied(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        if (hasPermission()) return false

        // If shouldShowRationale is false and we've requested before, it's permanent denial
        // Note: On first request, shouldShowRationale is also false, so we check if we've
        // ever gotten a result (which means we've requested before)
        return !shouldShowRationale() && permissionResult.value == false
    }

    /**
     * Ensures notification permission is granted before proceeding.
     * If permission is already granted, returns true immediately.
     * If permission needs to be requested, shows the system dialog and waits for result.
     *
     * This method is thread-safe and handles concurrent calls by using a mutex.
     * Only one permission request can be in flight at a time.
     *
     * Handles configuration changes by polling for permission status if the callback
     * doesn't fire within a reasonable time.
     *
     * When permission is denied, updates [denialState] to indicate whether the user
     * can be asked again (show rationale) or needs to go to settings (permanently denied).
     *
     * @return true if permission is granted, false otherwise
     */
    suspend fun ensurePermission(): Boolean {
        // Permission not needed on Android < 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        // Already granted
        if (hasPermission()) {
            _denialState.value = PermissionDenialState.None
            return true
        }

        // Use mutex to ensure only one permission request at a time
        return permissionMutex.withLock {
            // Check again inside the lock - permission might have been granted
            // by a concurrent request that just completed
            if (hasPermission()) {
                _denialState.value = PermissionDenialState.None
                return@withLock true
            }

            val launcher = permissionLauncher ?: return@withLock false

            // Reset the result before requesting
            permissionResult.value = null
            requestInFlight = true

            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)

            // Wait for the result using select to race between:
            // 1. The callback result (happy path - most common)
            // 2. A fallback periodic check for config change edge case
            val result = withTimeoutOrNull(PERMISSION_TIMEOUT_MS) {
                coroutineScope {
                    // Primary path: wait for the callback to fire
                    val callbackDeferred = async {
                        permissionResult.first { it != null }
                    }

                    // Fallback path: periodic check for config change case
                    // Only wakes up every 2 seconds as a safety net
                    val fallbackDeferred = async {
                        while (true) {
                            delay(CONFIG_CHANGE_CHECK_INTERVAL_MS)
                            // If permission was granted during config change
                            if (hasPermission()) {
                                return@async true
                            }
                            // If callback was lost (config change completed without result)
                            if (!requestInFlight && permissionResult.value == null) {
                                return@async hasPermission()
                            }
                        }
                        @Suppress("UNREACHABLE_CODE")
                        false
                    }

                    // Race: whichever completes first wins
                    select {
                        callbackDeferred.onAwait { it }
                        fallbackDeferred.onAwait { it }
                    }.also {
                        // Cancel the loser
                        callbackDeferred.cancel()
                        fallbackDeferred.cancel()
                    }
                }
            }
            requestInFlight = false

            val granted = result ?: false
            if (!granted) {
                // Update denial state based on whether we can ask again
                _denialState.value = if (shouldShowRationale()) {
                    PermissionDenialState.ShowRationale
                } else {
                    PermissionDenialState.PermanentlyDenied
                }
            } else {
                _denialState.value = PermissionDenialState.None
            }
            granted
        }
    }

    /**
     * Clears the denial state. Call when the user dismisses the permission dialog.
     */
    fun clearDenialState() {
        _denialState.value = PermissionDenialState.None
    }

    companion object {
        // 30 second timeout for permission dialog - generous but prevents infinite hang
        private const val PERMISSION_TIMEOUT_MS = 30_000L

        // Fallback check interval for config change edge case (rare)
        // Much less frequent than the previous 100ms polling
        private const val CONFIG_CHANGE_CHECK_INTERVAL_MS = 2_000L
    }
}

