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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/** Timeout in milliseconds to wait for a second tap before treating as single tap */
private const val DOUBLE_TAP_TIMEOUT_MS = 300L

/**
 * Modifier that handles common reader gestures:
 * - Pinch-to-zoom with relative scale factor
 * - Tap in left third of screen (e.g., previous page)
 * - Tap in right third of screen (e.g., next page)
 * - Tap in middle third of screen (e.g., toggle controls)
 *
 * @param containerSize The size of the container for calculating tap regions
 * @param consumeDoubleTaps If true, double-taps on left/right regions are not passed through
 *                          (tap callbacks are still fired). If false, double-taps are allowed
 *                          to pass through to underlying views (e.g., WebView for ReadAloud).
 * @param onZoomChange Callback during zoom gesture with relative scale (1.0 = no change)
 * @param onZoomEnd Callback when zoom gesture ends with final relative scale
 * @param onLeftTap Callback when user taps left third of screen
 * @param onRightTap Callback when user taps right third of screen
 * @param onMiddleTap Callback when user taps middle third of screen
 */
internal fun Modifier.readerGestures(
    containerSize: IntSize,
    consumeDoubleTaps: Boolean = true,
    onZoomChange: (scale: Double) -> Unit,
    onZoomEnd: (finalScale: Double) -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit,
    onMiddleTap: () -> Unit,
): Modifier = this.pointerInput(containerSize, consumeDoubleTaps) {
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

            val tapAction: () -> Unit = when {
                tapX < leftThird -> onLeftTap
                tapX > rightThird -> onRightTap
                else -> onMiddleTap
            }

            val isMiddleTap = tapX >= leftThird && tapX <= rightThird

            if (consumeDoubleTaps || isMiddleTap) {
                // Original behavior: fire tap immediately
                down.consume()
                tapAction()
            } else {
                // For ReadAloud: detect double-tap and don't fire left/right taps
                val timeSinceLastTap = currentTimeMs - lastTapTimeMs
                val isDoubleTap = timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS

                if (isDoubleTap) {
                    // This is a double-tap - cancel pending job and don't fire
                    pendingTapJob?.cancel()
                    pendingTapJob = null
                    lastTapTimeMs = 0L
                    // Don't consume - let it pass through to WebView
                } else {
                    // Might be first tap of double-tap, schedule with delay
                    lastTapTimeMs = currentTimeMs
                    pendingTapJob?.cancel()
                    pendingTapJob = scope.launch {
                        delay(DOUBLE_TAP_TIMEOUT_MS)
                        tapAction()
                    }
                }
            }
        }
    }
}

