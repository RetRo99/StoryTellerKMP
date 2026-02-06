package com.retro99.reader.ui.media

import org.readium.r2.shared.util.Url

/**
 * Represents a single text-audio synchronization point from SMIL.
 *
 * @param textHref The href of the XHTML file containing the text
 * @param fragmentId The ID of the span element to highlight (e.g., "s1", "s2")
 * @param audioHref The href of the audio file
 * @param startTime Start time in seconds
 * @param endTime End time in seconds
 */
data class MediaOverlayClip(
    val textHref: Url,
    val fragmentId: String?,
    val audioHref: Url,
    val startTime: Double,
    val endTime: Double,
)

