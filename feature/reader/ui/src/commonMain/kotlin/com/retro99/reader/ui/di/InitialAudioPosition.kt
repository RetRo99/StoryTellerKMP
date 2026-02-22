package com.retro99.reader.ui.di

/**
 * Wrapper for initial audio position from saved reading progress.
 * Used to inject the initial seek bar position into audio controllers.
 *
 * This is declared in the ReaderScope by the ViewModel when opening a publication,
 * using the saved position's audioTimestampMs and href values.
 *
 * @param positionMs The initial audio position in milliseconds, or null if not available
 * @param href The initial chapter href for audio initialization, or null if not available
 */
data class InitialAudioPosition(
    val positionMs: Long?,
    val href: String?,
)

