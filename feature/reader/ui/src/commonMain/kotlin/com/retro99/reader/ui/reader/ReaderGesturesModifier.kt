package com.retro99.reader.ui.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize

/**
 * Modifier that handles common reader gestures:
 * - Pinch-to-zoom with relative scale factor
 * - Tap in left third of screen (e.g., previous page)
 * - Tap in right third of screen (e.g., next page)
 * - Tap in middle third of screen (e.g., toggle controls)
 *
 * @param containerSize The size of the container for calculating tap regions
 * @param onZoomChange Callback during zoom gesture with relative scale (1.0 = no change)
 * @param onZoomEnd Callback when zoom gesture ends with final relative scale
 * @param onLeftTap Callback when user taps left third of screen
 * @param onRightTap Callback when user taps right third of screen
 * @param onMiddleTap Callback when user taps middle third of screen
 */
internal fun Modifier.readerGestures(
    containerSize: IntSize,
    onZoomChange: (scale: Double) -> Unit,
    onZoomEnd: (finalScale: Double) -> Unit,
    onLeftTap: () -> Unit,
    onRightTap: () -> Unit,
    onMiddleTap: () -> Unit,
): Modifier = this.pointerInput(containerSize) {
    val touchSlop = viewConfiguration.touchSlop
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

            down.consume()
            when {
                tapX < leftThird -> onLeftTap()
                tapX > rightThird -> onRightTap()
                else -> onMiddleTap()
            }
        }
    }
}

