package com.retro99.reader.ui.audiobook

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.getOrElse
import com.retro99.base.nowMillis
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.model.BookType
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import com.retro99.reader.domain.ReaderSettingsRepository
import com.retro99.reader.domain.model.PositionDomainModel
import com.retro99.reader.domain.model.ReadingProgressResult
import com.retro99.reader.domain.usecase.GetReadingProgressWithConflictUseCase
import com.retro99.reader.domain.usecase.SaveReadingProgressUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import java.io.File

@KoinViewModel
class AudiobookPlayerViewModel(
    @InjectedParam private val serverId: String,
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val onClose: () -> Unit,
    @Provided private val context: Context,
    @Provided private val readerSettingsRepository: ReaderSettingsRepository,
    @Provided private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    @Provided private val getReadingProgressWithConflictUseCase: GetReadingProgressWithConflictUseCase,
    @Provided private val getBookByUuidUseCase: GetBookByUuidUseCase,
) : BaseViewModel<AudiobookPlayerViewState, AudiobookPlayerIntent>(
    AudiobookPlayerViewState()
) {

    private val player: ExoPlayer
    private var positionUpdateJob: Job? = null
    private var hasRestoredPosition = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState { it.copy(playbackState = playbackState) }
                if (playbackState == Player.STATE_READY) {
                    updateState {
                        it.copy(
                            isLoading = false,
                            totalDurationMs = player.duration.coerceAtLeast(0L),
                            trackCount = player.mediaItemCount,
                        )
                    }
                    if (!hasRestoredPosition) {
                        hasRestoredPosition = true
                        restoreProgress()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                    saveProgress()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val trackIndex = player.currentMediaItemIndex
                updateState { it.copy(currentTrackIndex = trackIndex) }
                saveProgress()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Logger.e(error) { "Audiobook playback error" }
                updateState { it.copy(error = error.message ?: "Playback error", isLoading = false) }
            }
        })

        loadAudioFiles()
    }

    private fun loadAudioFiles() {
        viewModelScope.launch {
            val bookResult = getBookByUuidUseCase(serverId, bookUuid).first()
            val book = bookResult.getOrElse { null }
            updateState {
                it.copy(
                    bookTitle = book?.title ?: "",
                    bookCoverUrl = book?.coverUrl,
                )
            }

            val result = readerSettingsRepository.prepareEbook(bookUuid, "", BookType.AUDIOBOOK)
            val cachedPath = result.getOrElse { null }
            if (cachedPath == null) {
                updateState { it.copy(error = "Audio files not found", isLoading = false) }
                return@launch
            }

            val dir = File(cachedPath)
            if (!dir.exists() || !dir.isDirectory) {
                updateState { it.copy(error = "Audio directory not found", isLoading = false) }
                return@launch
            }

            val audioFiles = dir.listFiles()
                ?.filter { it.isFile }
                ?.sortedBy { it.name }
                ?: emptyList()

            if (audioFiles.isEmpty()) {
                updateState { it.copy(error = "No audio files found", isLoading = false) }
                return@launch
            }

            val mediaItems = audioFiles.map { file ->
                MediaItem.Builder()
                    .setUri(file.toURI().toString())
                    .setMediaId(file.name)
                    .build()
            }

            val trackTitles = audioFiles.map { file ->
                file.nameWithoutExtension.replace(Regex("^\\d+\\s*"), "")
                    .ifEmpty { file.name }
            }

            updateState {
                it.copy(
                    trackTitles = trackTitles,
                    trackCount = mediaItems.size,
                )
            }

            player.setMediaItems(mediaItems)
            player.prepare()
        }
    }

    private fun restoreProgress() {
        viewModelScope.launch {
            try {
                val totalDuration = player.duration
                if (totalDuration <= 0) return@launch

                val savedPosition = loadSavedPosition()
                savedPosition?.audioTimestampMs?.let { timestampMs ->
                    val trackIndex = savedPosition.chapterIndex ?: 0
                    if (trackIndex in 0 until player.mediaItemCount) {
                        player.seekTo(trackIndex, timestampMs)
                        updateState {
                            it.copy(
                                currentTrackIndex = trackIndex,
                                currentPositionMs = timestampMs,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.w(e) { "Failed to restore audiobook progress" }
            }
        }
    }

    private suspend fun loadSavedPosition(): PositionDomainModel? {
        return try {
            getReadingProgressWithConflictUseCase(serverId, bookUuid)
                .getOrElse { null }
                ?.let { result ->
                    when (result) {
                        is ReadingProgressResult.Resolved -> result.position
                        is ReadingProgressResult.Conflict -> result.localPosition
                    }
                }
        } catch (e: Exception) {
            null
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                val position = player.currentPosition.coerceAtLeast(0L)
                updateState { it.copy(currentPositionMs = position) }
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun saveProgress() {
        viewModelScope.launch {
            try {
                val position = player.currentPosition.coerceAtLeast(0L)
                val trackIndex = player.currentMediaItemIndex
                val totalDuration = player.duration.coerceAtLeast(0L)
                val totalProgression = if (totalDuration > 0) {
                    position.toDouble() / totalDuration.toDouble()
                } else {
                    null
                }

                val progress = PositionDomainModel(
                    bookUuid = bookUuid,
                    serverId = serverId,
                    timestamp = nowMillis(),
                    createdAt = null,
                    updatedAt = null,
                    locatorHref = null,
                    locatorType = null,
                    locatorTitle = null,
                    locatorTarget = null,
                    audioTimestampMs = position,
                    chapterIndex = trackIndex,
                    progression = totalProgression,
                    totalChapters = player.mediaItemCount,
                    totalDurationMs = if (totalDuration > 0) totalDuration else null,
                    totalProgression = totalProgression,
                    position = null,
                )
                saveReadingProgressUseCase(progress)
            } catch (e: Exception) {
                Logger.w(e) { "Failed to save audiobook progress" }
            }
        }
    }

    override fun onIntent(intent: AudiobookPlayerIntent) {
        when (intent) {
            AudiobookPlayerIntent.PlayPauseClicked -> {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }

            AudiobookPlayerIntent.SkipForwardClicked -> {
                player.seekForward()
            }

            AudiobookPlayerIntent.SkipBackwardClicked -> {
                player.seekBack()
            }

            AudiobookPlayerIntent.NextTrackClicked -> {
                player.seekToNextMediaItem()
            }

            AudiobookPlayerIntent.PreviousTrackClicked -> {
                if (player.currentPosition > TRACK_RESET_THRESHOLD_MS) {
                    player.seekTo(0)
                } else {
                    player.seekToPreviousMediaItem()
                }
            }

            is AudiobookPlayerIntent.SeekTo -> {
                player.seekTo(intent.positionMs)
                updateState { it.copy(currentPositionMs = intent.positionMs) }
            }

            is AudiobookPlayerIntent.SelectTrack -> {
                if (intent.trackIndex in 0 until player.mediaItemCount) {
                    player.seekTo(intent.trackIndex, 0)
                    player.play()
                }
            }

            is AudiobookPlayerIntent.PlaybackSpeedChanged -> {
                player.playbackParameters = PlaybackParameters(intent.speed)
                updateState { it.copy(playbackSpeed = intent.speed) }
            }
        }
    }

    fun close() {
        player.pause()
        saveProgress()
        viewModelScope.launch {
            delay(SAVE_DELAY_MS)
            player.release()
            onClose()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        player.release()
    }

    companion object {
        private const val SEEK_INCREMENT_MS = 10_000L
        private const val POSITION_UPDATE_INTERVAL_MS = 500L
        private const val TRACK_RESET_THRESHOLD_MS = 3_000L
        private const val SAVE_DELAY_MS = 300L
    }
}
