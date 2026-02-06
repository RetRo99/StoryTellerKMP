package com.retro99.reader.ui.model

/**
 * Data class representing the current audio position state for ReadAloud books.
 *
 * @param currentPositionMs The current playback position in milliseconds
 * @param totalDurationMs The total duration of the current audio segment in milliseconds, or null if unknown
 */
data class AudioPositionState(
    val currentPositionMs: Long,
    val totalDurationMs: Long?,
)

