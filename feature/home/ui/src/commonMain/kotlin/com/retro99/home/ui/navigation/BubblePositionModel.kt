package com.retro99.home.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Serializable model for persisting the bubble position.
 *
 * @property isOnEndSide Whether the bubble is pinned to the end (right) side. False means start (left) side.
 * @property yFraction The vertical position as a fraction of screen height (0.0 to 1.0).
 */
@Serializable
data class BubblePositionModel(
    val isOnEndSide: Boolean = true,
    val yFraction: Float = 0.7f,
) {
    fun toBubbleSide(): BubbleSide = if (isOnEndSide) BubbleSide.END else BubbleSide.START

    companion object {
        val DEFAULT = BubblePositionModel()

        fun fromBubbleSide(side: BubbleSide, yFraction: Float): BubblePositionModel {
            return BubblePositionModel(
                isOnEndSide = side == BubbleSide.END,
                yFraction = yFraction,
            )
        }
    }
}

