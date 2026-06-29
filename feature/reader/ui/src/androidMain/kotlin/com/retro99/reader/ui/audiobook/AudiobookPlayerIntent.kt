package com.retro99.reader.ui.audiobook

import com.retro99.base.ui.BaseIntent

sealed interface AudiobookPlayerIntent : BaseIntent {
    data object PlayPauseClicked : AudiobookPlayerIntent
    data object SkipForwardClicked : AudiobookPlayerIntent
    data object SkipBackwardClicked : AudiobookPlayerIntent
    data object NextTrackClicked : AudiobookPlayerIntent
    data object PreviousTrackClicked : AudiobookPlayerIntent
    data class SeekTo(val positionMs: Long) : AudiobookPlayerIntent
    data class SelectTrack(val trackIndex: Int) : AudiobookPlayerIntent
    data class PlaybackSpeedChanged(val speed: Float) : AudiobookPlayerIntent
}
