package com.retro99.books.data

import org.readium.r2.shared.publication.Publication

/**
 * Extension functions for Readium's Publication class.
 */

/**
 * Checks if this publication has media overlays (audio narration).
 *
 * This function checks three indicators:
 * 1. **Duration metadata**: Must be > 0 (not just non-null), as some EPUBs have duration=0.0 without actual audio
 * 2. **Audio resources**: Presence of audio files in the publication
 * 3. **SMIL resources**: Presence of SMIL files (Synchronized Multimedia Integration Language)
 *
 * Returns true if ANY of these conditions are met.
 *
 * @return true if the publication has media overlays, false otherwise
 */
fun Publication.hasMediaOverlays(): Boolean {
    val duration = metadata.duration
    val hasDuration = duration != null && duration > 0.0
    val hasAudioResource = resources.any { it.mediaType?.isAudio == true }
    val hasSmilResource = resources.any {
        it.mediaType?.toString()?.contains("smil") == true ||
                it.href.toString().endsWith(".smil")
    }

    return hasDuration || hasAudioResource || hasSmilResource
}

