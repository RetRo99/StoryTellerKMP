package com.retro99.reader.ui.playback

import android.util.Log
import androidx.media3.common.MediaMetadata

internal const val MAX_EMBEDDED_ARTWORK_BYTES = 256 * 1024

internal fun MediaMetadata.Builder.setArtworkDataIfSmall(
    artwork: ByteArray?,
    logTag: String,
): MediaMetadata.Builder {
    if (artwork == null) return this
    if (artwork.size > MAX_EMBEDDED_ARTWORK_BYTES) {
        Log.w(
            logTag,
            "Skipping embedded artwork (${artwork.size} bytes); Media3 clones metadata " +
                "for controller updates and large artwork can OOM low-memory devices.",
        )
        return this
    }
    return setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
}
