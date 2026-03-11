package com.retro99.reader.ui.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize
import co.touchlab.kermit.Logger
import com.retro99.base.nowMillis
import com.retro99.reader.ui.navigator.DOUBLE_TAP_TIMEOUT_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

private val logger = Logger.withTag("testingtaps")

/**
 * Modifier that handles common reader gestures:
 * - Pinch-to-zoom with relative scale factor
 * - Tap in left third of screen (e.g., previous page)
 * - Tap in right third of screen (e.g., next page)
 * - Tap in middle third of screen (e.g., toggle controls)
 * - Double-tap detection (when detectDoubleTaps is true)
 *
 * @param containerSize The size of the container for calculating tap regions
 * @param detectDoubleTaps If true, waits DOUBLE_TAP_TIMEOUT_MS before firing single taps
 *                         to detect double-taps. If false, taps fire immediately.
 * @param onZoomChange Callback during zoom gesture with relative scale (1.0 = no change)
 * @param onZoomEnd Callback when zoom gesture ends with final relative scale
 * @param onLeftTap Callback when user taps left third of screen
 * @param onRightTap Callback when user taps right third of screen
 * @param onMiddleTap Callback when user taps middle third of screen
 * @param onDoubleTap Callback when user double-taps (only called when detectDoubleTaps is true)
 */
internal fun Modifier.readerGestures(
    containerSize: IntSize,
    detectDoubleTaps: Boolean = false,
    onZoomChange: (scale: Double) -> Unit,
    onZoomEnd: (finalScale: Double) -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit,
    onMiddleTap: () -> Unit,
    onDoubleTap: () -> Unit = {},
): Modifier = this.pointerInput(containerSize, detectDoubleTaps) {
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

            logger.d { "Tap detected: detectDoubleTaps=$detectDoubleTaps, isMiddleTap=$isMiddleTap, tapX=$tapX" }

            if (!detectDoubleTaps) {
                // Original behavior: fire tap immediately
                logger.d { "Firing tap immediately (detectDoubleTaps=false)" }
                down.consume()
                tapAction()
            } else {
                // Double-tap detection enabled: wait before firing single taps
                val timeSinceLastTap = currentTimeMs - lastTapTimeMs
                val isDoubleTap = timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS

                logger.d { "Double-tap check: timeSinceLastTap=${timeSinceLastTap}ms, timeout=${DOUBLE_TAP_TIMEOUT_MS}ms, isDoubleTap=$isDoubleTap" }

                if (isDoubleTap) {
                    // This is a double-tap - cancel pending single tap and fire double-tap callback
                    logger.d { "Double-tap detected! Cancelling pending job and firing onDoubleTap" }
                    pendingTapJob?.cancel()
                    pendingTapJob = null
                    lastTapTimeMs = 0L
                    down.consume()
                    onDoubleTap()
                } else {
                    // Might be first tap of double-tap, schedule with delay
                    logger.d { "First tap - scheduling single tap action with ${DOUBLE_TAP_TIMEOUT_MS}ms delay" }
                    lastTapTimeMs = currentTimeMs
                    pendingTapJob?.cancel()
                    pendingTapJob = scope.launch {
                        delay(DOUBLE_TAP_TIMEOUT_MS)
                        logger.d { "Delay elapsed - firing single tap action now" }
                        tapAction()
                    }
                }
            }
        }
    }
}

