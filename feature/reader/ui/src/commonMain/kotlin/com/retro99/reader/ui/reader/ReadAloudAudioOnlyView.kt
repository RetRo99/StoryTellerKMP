package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.audiobook.AudioPlayerCallbacks
import com.retro99.reader.ui.audiobook.AudioPlayerScreen
import com.retro99.reader.ui.audiobook.AudioPlayerState

@Composable
internal fun ReadAloudAudioOnlyView(
    viewState: ReaderViewState,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    onExit: () -> Unit,
) {
    val backHandlerState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backHandlerState,
        onBackCompleted = { onExit() },
    )

    KeepScreenOn(enabled = viewState.isPlaying)

    val chapters = viewState.tableOfContents
    val currentChapterHref = viewState.currentPosition?.href
    val currentChapterIndex = chapters.indexOfFirst { it.href == currentChapterHref }

    val audioPlayerState = AudioPlayerState(
        bookUuid = viewState.bookUuid,
        bookTitle = viewState.bookTitle,
        bookCoverUrl = viewState.bookCoverUrl,
        isPlaying = viewState.isPlaying,
        isBuffering = !viewState.isAudioPlayerReady,
        isLoading = false,
        currentPositionMs = viewState.currentAudioPositionMs,
        totalDurationMs = viewState.totalDurationMs ?: 0L,
        currentTrackIndex = currentChapterIndex,
        trackCount = chapters.size,
        trackTitles = chapters.map { it.title },
        playbackSpeed = viewState.currentSettings?.playbackSpeed ?: 1.0f,
        error = null,
    )

    val callbacks = AudioPlayerCallbacks(
        onPlayPause = { intentDispatcher(ReaderIntent.TogglePlayback) },
        onSkipForward = { intentDispatcher(ReaderIntent.SkipForward()) },
        onSkipBackward = { intentDispatcher(ReaderIntent.SkipBackward()) },
        onPreviousTrack = { intentDispatcher(ReaderIntent.GoToPreviousChapterAndPlay) },
        onNextTrack = { intentDispatcher(ReaderIntent.GoToNextChapterAndPlay) },
        onSeek = { positionMs -> intentDispatcher(ReaderIntent.SeekTo(positionMs)) },
        onSelectTrack = { index ->
            val chapter = chapters.getOrNull(index)
            if (chapter != null) {
                intentDispatcher(ReaderIntent.GoToChapterAndPlay(chapter.href, viewState.currentPosition))
            }
        },
        onSpeedChange = { speed -> intentDispatcher(ReaderIntent.SetPlaybackSpeed(speed)) },
        onClose = onExit,
    )

    AudioPlayerScreen(
        state = audioPlayerState,
        callbacks = callbacks,
        titleFallback = "Read Aloud",
        onBack = onExit,
    )
}
