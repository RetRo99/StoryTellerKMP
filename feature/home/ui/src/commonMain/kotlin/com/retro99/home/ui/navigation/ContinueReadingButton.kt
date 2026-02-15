package com.retro99.home.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.compose.CoilImage

/**
 * Size of the floating bubble in dp.
 */
private val BUBBLE_SIZE = 56.dp

/**
 * A circular floating bubble that shows the currently reading book cover.
 * Designed to be used with [DraggableFloatingBubble] for drag-to-pin functionality.
 * Tapping it navigates directly to the reader.
 */
@Composable
fun ContinueReadingBubble(
    currentlyReading: CurrentlyReadingUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(BUBBLE_SIZE)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            if (currentlyReading.coverUrl != null) {
                // Show book cover filling the bubble
                CoilImage(
                    data = currentlyReading.coverUrl,
                    cacheKey = "continue_reading_bubble_${currentlyReading.bookUuid}",
                    contentDescription = currentlyReading.bookTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(BUBBLE_SIZE)
                        .clip(CircleShape),
                )
            } else {
                // Fallback to book icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = currentlyReading.bookTitle,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

