package com.retro99.home.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Represents which side of the screen the bubble is pinned to.
 */
enum class BubbleSide {
    START,
    END,
}

/**
 * A draggable floating bubble that can be moved around the screen and pins to either
 * the left or right edge when released. Similar to Facebook Messenger chat heads or
 * iOS AssistiveTouch.
 *
 * @param modifier Modifier for the bubble container
 * @param initialSide Which side the bubble starts on
 * @param initialYFraction Initial vertical position as a fraction of screen height (0.0 to 1.0)
 * @param edgePadding Padding from the edge when pinned (in pixels)
 * @param onPositionChanged Callback when the bubble position changes (after drag ends)
 * @param content The content to display inside the bubble
 */
@Composable
fun DraggableFloatingBubble(
    modifier: Modifier = Modifier,
    initialSide: BubbleSide = BubbleSide.END,
    initialYFraction: Float = 0.7f,
    edgePadding: Float = 16f,
    onPositionChanged: ((side: BubbleSide, yFraction: Float) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    // Container size (the parent Box)
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // Bubble size
    var bubbleSize by remember { mutableStateOf(IntSize.Zero) }

    // Current pinned side
    var pinnedSide by remember(initialSide) { mutableStateOf(initialSide) }

    // Whether we're currently dragging
    var isDragging by remember { mutableStateOf(false) }

    // Raw drag offset (used during drag)
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // Calculate the target X position based on pinned side
    val targetX = remember(pinnedSide, containerSize, bubbleSize, edgePadding) {
        when (pinnedSide) {
            BubbleSide.START -> edgePadding
            BubbleSide.END -> (containerSize.width - bubbleSize.width - edgePadding).coerceAtLeast(0f)
        }
    }

    // Calculate Y position from fraction - recalculates when container size or initial fraction changes
    val targetYFromFraction = remember(containerSize, bubbleSize, initialYFraction) {
        val maxY = (containerSize.height - bubbleSize.height).coerceAtLeast(0)
        (maxY * initialYFraction).coerceIn(0f, maxY.toFloat())
    }

    // Target Y position - starts from fraction, updated on drag
    var targetY by remember { mutableFloatStateOf(targetYFromFraction) }

    // Keep targetY in sync with fraction when not dragging and container size changes
    LaunchedEffect(targetYFromFraction) {
        if (!isDragging) {
            targetY = targetYFromFraction
        }
    }

    // Position is ready when both container and bubble have been measured
    val isPositionReady = containerSize.width > 0 && containerSize.height > 0 &&
        bubbleSize.width > 0 && bubbleSize.height > 0

    // Use Animatable for control over initial snap vs animated transitions
    val animatedX = remember { Animatable(0f) }
    val animatedY = remember { Animatable(0f) }

    // Track if we've done the initial positioning
    var hasInitialized by remember { mutableStateOf(false) }

    // Handle position updates
    LaunchedEffect(isPositionReady, isDragging, targetX, targetY, dragOffsetX, dragOffsetY) {
        if (!isPositionReady) return@LaunchedEffect

        val newTargetX = if (isDragging) dragOffsetX else targetX
        val newTargetY = if (isDragging) dragOffsetY else targetY

        if (!hasInitialized) {
            // First time: snap to position without animation
            animatedX.snapTo(newTargetX)
            animatedY.snapTo(newTargetY)
            hasInitialized = true
        } else if (isDragging) {
            // During drag: snap to position immediately for responsive feel
            animatedX.snapTo(newTargetX)
            animatedY.snapTo(newTargetY)
        } else {
            // After drag ends: animate to final position
            launch {
                animatedX.animateTo(
                    newTargetX,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
            launch {
                animatedY.animateTo(
                    newTargetY,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { containerSize = it }
    ) {
        // Always compose the bubble so it can be measured, but hide until initialized
        Box(
            modifier = Modifier
                .onSizeChanged { bubbleSize = it }
                .alpha(if (hasInitialized) 1f else 0f)
                .offset { IntOffset(animatedX.value.roundToInt(), animatedY.value.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffsetX = animatedX.value
                            dragOffsetY = animatedY.value
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Update drag position with bounds checking
                            val maxX = (containerSize.width - bubbleSize.width).coerceAtLeast(0)
                            val maxY = (containerSize.height - bubbleSize.height).coerceAtLeast(0)
                            dragOffsetX = (dragOffsetX + dragAmount.x).coerceIn(0f, maxX.toFloat())
                            dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(0f, maxY.toFloat())
                        },
                        onDragEnd = {
                            isDragging = false
                            // Determine which side to pin to based on current position
                            val centerX = dragOffsetX + bubbleSize.width / 2
                            val screenCenterX = containerSize.width / 2
                            val newSide = if (centerX < screenCenterX) BubbleSide.START else BubbleSide.END
                            pinnedSide = newSide
                            // Keep the Y position where user released
                            targetY = dragOffsetY
                            // Calculate Y fraction and notify callback
                            val maxY = (containerSize.height - bubbleSize.height).coerceAtLeast(1)
                            val yFraction = (dragOffsetY / maxY).coerceIn(0f, 1f)
                            onPositionChanged?.invoke(newSide, yFraction)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                    )
                },
            content = content,
        )
    }
}

