package com.retro99.reader.ui.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize
import com.retro99.base.nowMillis
import com.retro99.reader.domain.model.ReaderSettingsDomainModel.Companion.DEFAULT_DOUBLE_TAP_TIMEOUT_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Modifier that handles common reader gestures:
 * - Pinch-to-zoom with relative scale factor
 * - Tap in left third of screen (e.g., previous page)
 * - Tap in right third of screen (e.g., next page)
 * - Tap in middle third of screen (e.g., toggle controls)
 * - Double-tap detection (when detectDoubleTaps is true)
 *
 * @param containerSize The size of the container for calculating tap regions
 * @param detectDoubleTaps If true, waits doubleTapTimeoutMs before firing single taps
 *                         to detect double-taps. If false, taps fire immediately.
 * @param doubleTapTimeoutMs Timeout in milliseconds to wait for a second tap before treating as single tap.
 *                           Only used when detectDoubleTaps is true.
 * @param tapNavigationEnabled If false, left/right taps are disabled (middle tap still works)
 * @param onZoomChange Callback during zoom gesture with relative scale (1.0 = no change)
 * @param onZoomEnd Callback when zoom gesture ends with final relative scale
 * @param onLeftTap Callback when user taps left third of screen (only called if tapNavigationEnabled)
 * @param onRightTap Callback when user taps right third of screen (only called if tapNavigationEnabled)
 * @param onMiddleTap Callback when user taps middle third of screen
 * @param onDoubleTap Callback when user double-taps (only called when detectDoubleTaps is true)
 */
internal fun Modifier.readerGestures(
    containerSize: IntSize,
    detectDoubleTaps: Boolean = false,
    doubleTapTimeoutMs: Int = DEFAULT_DOUBLE_TAP_TIMEOUT_MS,
    tapNavigationEnabled: Boolean = true,
    onZoomChange: (scale: Double) -> Unit,
    onZoomEnd: (finalScale: Double) -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit,
    onMiddleTap: () -> Unit,
    onDoubleTap: () -> Unit = {},
): Modifier = this.pointerInput(containerSize, detectDoubleTaps, doubleTapTimeoutMs, tapNavigationEnabled) {
    val touchSlop = viewConfiguration.touchSlop
    var lastTapTimeMs = 0L
    var pendingTapJob: Job? = null
    val scope = CoroutineScope(coroutineContext)

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val downPosition = down.position
        var zoomAccumulator = 1f
        var gestureActive = false
        var isSingleTap = true

        do {
            val event = awaitPointerEvent(PointerEventPass.Main)
            if (event.changes.size >= 2) {
                isSingleTap = false
                val zoomChange = event.calculateZoom()

                if (!gestureActive) {
                    zoomAccumulator *= zoomChange
                    val isZoomingOut = zoomAccumulator < 0.80f
                    val isZoomingIn = zoomAccumulator > 1.20f

                    if (isZoomingIn || isZoomingOut) {
                        gestureActive = true
                        onZoomChange(zoomAccumulator.toDouble())
                    }
                } else {
                    if (zoomChange != 1f) {
                        zoomAccumulator *= zoomChange
                        onZoomChange(zoomAccumulator.toDouble())
                    }
                    event.changes.forEach {
                        if (it.positionChanged()) it.consume()
                    }
                }
            } else if (isSingleTap) {
                // Check if any pointer moved beyond touch slop threshold
                val hasDragged = event.changes.any { change ->
                    val distance = (change.position - downPosition).getDistance()
                    distance > touchSlop
                }
                if (hasDragged) {
                    isSingleTap = false
                }
            }
        } while (event.changes.any { it.pressed })

        // Gesture ended
        if (gestureActive) {
            onZoomEnd(zoomAccumulator.toDouble())
        }

        // Handle taps for page navigation and controls toggle
        if (isSingleTap && containerSize.width > 0) {
            val tapX = down.position.x
            val leftThird = containerSize.width / 3f
            val rightThird = containerSize.width * 2f / 3f
            val currentTimeMs = nowMillis()

            // Determine tap action based on region and whether tap navigation is enabled
            val tapAction: (() -> Unit)? = when {
                tapX < leftThird -> if (tapNavigationEnabled) onLeftTap else null
                tapX > rightThird -> if (tapNavigationEnabled) onRightTap else null
                else -> onMiddleTap // Middle tap always works (toggles controls)
            }

            // If no action (tap navigation disabled for left/right), do nothing
            if (tapAction == null) {
                return@awaitEachGesture
            }

            if (!detectDoubleTaps) {
                // Original behavior: fire tap immediately
                down.consume()
                tapAction()
            } else {
                // Double-tap detection enabled: wait before firing single taps
                val timeSinceLastTap = currentTimeMs - lastTapTimeMs
                val isDoubleTap = timeSinceLastTap < doubleTapTimeoutMs

                if (isDoubleTap) {
                    // This is a double-tap - cancel pending single tap and fire double-tap callback
                    pendingTapJob?.cancel()
                    pendingTapJob = null
                    lastTapTimeMs = 0L
                    down.consume()
                    onDoubleTap()
                } else {
                    // Might be first tap of double-tap, schedule with delay
                    lastTapTimeMs = currentTimeMs
                    pendingTapJob?.cancel()
                    pendingTapJob = scope.launch {
                        delay(doubleTapTimeoutMs.toLong())
                        tapAction()
                    }
                }
            }
        }
    }
}

