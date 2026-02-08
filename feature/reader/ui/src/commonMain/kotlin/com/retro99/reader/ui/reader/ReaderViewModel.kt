package com.retro99.reader.ui.reader

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.now
import com.retro99.base.nowMillis
import com.retro99.base.result.AppError
import com.retro99.base.ui.BaseViewModel
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.PositionDomainModel
import com.retro99.reader.domain.model.ReaderInitializationData
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.InitializeReaderUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReadingProgressUseCase
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.toDomainModel
import com.retro99.reader.ui.model.toUiData
import com.retro99.reader.ui.model.toUiModel
import com.retro99.reader.ui.navigator.EpubNavigatorControllerNew
import com.retro99.reader.ui.service.EpubPublicationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class ReaderViewModel(
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val bookType: BookType,
    @InjectedParam private val onClose: () -> Unit,
    @Provided private val initializeReaderUseCase: InitializeReaderUseCase,
    @Provided private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @Provided private val publicationService: EpubPublicationService,
    @Provided val bookController: EpubNavigatorControllerNew,
) : BaseViewModel<ReaderViewState, ReaderIntent>(
    ReaderViewState(
        bookUuid = bookUuid,
        bookType = bookType,
    )
) {

    private val _commands = MutableSharedFlow<ReaderCommand>()
    val commands: SharedFlow<ReaderCommand> = _commands.asSharedFlow()

    init {
        addCloseable(bookController)
        initializeReader()
        observeSettingsChanges()
    }

    override fun onIntent(intent: ReaderIntent) {
        println("čič $intent")
        when (intent) {
            is ReaderIntent.UpdatePosition -> updatePosition(intent.position)
            is ReaderIntent.UpdateSettings -> updateSettings(intent.settings)
            ReaderIntent.ToggleSettings -> toggleSettings()
            ReaderIntent.Close -> close()
            ReaderIntent.UseLocalPosition -> resolveConflictWithLocal()
            ReaderIntent.UseRemotePosition -> resolveConflictWithRemote()
            ReaderIntent.GoToNextPage -> goToNextPage()
            ReaderIntent.GoToPreviousPage -> goToPreviousPage()
            ReaderIntent.TogglePlayback -> togglePlayback()
            is ReaderIntent.SeekTo -> seekTo(intent.audioTimestampMs)
            is ReaderIntent.SetPlaybackSpeed -> setPlaybackSpeed(intent.speed)
            is ReaderIntent.SkipForward -> skipForward(intent.milliseconds)
            is ReaderIntent.SkipBackward -> skipBackward(intent.milliseconds)
            is ReaderIntent.UpdateAudioPosition -> updateAudioPosition(
                intent.positionMs,
                intent.totalDurationMs
            )

            is ReaderIntent.UpdatePlayingState -> updatePlayingState(intent.isPlaying)
            ReaderIntent.MediaPlayerReady -> updateState { it.copy(isAudioPlayerReady = true) }
        }
    }

    private fun goToNextPage() {
        bookController.goToNextPage()
    }

    private fun goToPreviousPage() {
        bookController.goToPreviousPage()
    }

    private fun observeSettingsChanges() {
        getReaderSettingsUseCase()
            .onEach { settings ->
                bookController.setSettings(settings.toUiModel())
            }
            .launchIn(viewModelScope)
    }

    private fun initializeReader() {
        viewModelScope.launch {
            initializeReaderUseCase(bookUuid, bookType)
                .onSuccess { data ->
                    openPublication(data)
                }
                .onFailure { error ->
                    updateState { it.copy(error = error) }
                }
        }
    }

    private suspend fun openPublication(data: ReaderInitializationData) {
        val settings = data.initialSettings.toUiModel()
        val bookType = data.bookType
        val (position, conflict) = data.progressResult.toUiData()

        println("čič openPublication: initialAudioTimestampMs=${position?.audioTimestampMs}")

        val publication = publicationService.openPublication(
            filePath = data.localEbookPath,
            initialSettings = settings,
            bookType = bookType,
            initialPosition = position,
        )

        publication?.let {
            updateState { state ->
                state.copy(
                    bookUuid = data.bookUuid,
                    publication = it,
                    bookType = bookType,
                    positionConflict = conflict,
                    playbackSpeed = settings.playbackSpeed,
                    error = null,
                    initialAudioPositionMs = position?.audioTimestampMs,
                    currentAudioPositionMs = position?.audioTimestampMs ?: 0L,
                )
            }
        }
            ?: updateState { it.copy(error = AppError.UnknownError(Throwable("Failed to open publication"))) }
    }

    private fun resolveConflictWithLocal() {
        val conflict = viewState.value.positionConflict ?: return
        viewModelScope.launch {
            updateState { it.copy(positionConflict = null) }
            bookController.goToPosition(conflict.localPosition)
        }
    }

    private fun resolveConflictWithRemote() {
        val conflict = viewState.value.positionConflict ?: return
        viewModelScope.launch {
            updateState { it.copy(positionConflict = null) }
            bookController.goToPosition(conflict.remotePosition)
        }
    }

    private fun updatePosition(position: PositionUiModel) {
        if (viewState.value.positionConflict != null) return

        updateState { it.copy(lastKnownPosition = position) }

        val now = now().toString()
        val currentState = viewState.value
        val audioTimestamp = currentState.currentAudioPositionMs.takeIf { it > 0 }
        val positionDomainModel = PositionDomainModel(
            bookUuid = bookUuid,
            timestamp = nowMillis(),
            createdAt = position.createdAt,
            updatedAt = now,
            locatorHref = position.href,
            locatorType = position.type,
            locatorTitle = position.title,
            locatorTarget = null,
            audioTimestampMs = audioTimestamp,
            chapterIndex = position.chapterIndex,
            progression = position.progression,
            totalChapters = position.totalChapters,
            totalDurationMs = currentState.totalDurationMs,
            totalProgression = position.totalProgression,
            position = position.position,
        )

        viewModelScope.launch {
            saveReadingProgressUseCase(positionDomainModel)
        }
    }

    private fun updateSettings(settings: ReaderSettingsUiModel) {
        viewModelScope.launch {
            saveReaderSettingsUseCase(settings.toDomainModel())
        }
    }

    private fun toggleSettings() {
        updateState { it.copy(isSettingsVisible = !it.isSettingsVisible) }
    }

    private fun close() {
        if (viewState.value.isReadAloud) {
            saveCurrentAudioPosition()
        }
        onClose()
    }

    /**
     * Toggles audio playback.
     *
     * IMPORTANT: We do NOT optimistically update isPlaying here. The player is the single
     * source of truth for playback state. The UI will update when the player reports its
     * actual state via UpdatePlayingState intent. This prevents state divergence when:
     * - Permission check fails
     * - Audio focus acquisition fails
     * - Foreground service fails to start
     * - Any other playback initialization error occurs
     */
    private fun togglePlayback() {
        val currentState = viewState.value
        val isCurrentlyPlaying = currentState.isPlaying
        viewModelScope.launch {
            if (isCurrentlyPlaying) {
                _commands.emit(ReaderCommand.PausePlayback)
                // Note: isPlaying will be updated via UpdatePlayingState when player confirms
            } else {
                if (!currentState.hasStartedPlayback) {
                    // First playback - use StartPlayback to prepare the chapter
                    // Pass initial position if available from saved reading progress
                    _commands.emit(ReaderCommand.StartPlayback(currentState.initialAudioPositionMs))
                    // Mark that we've attempted playback (for initial position handling)
                    // but don't set isPlaying - wait for player confirmation
                    updateState { it.copy(hasStartedPlayback = true) }
                } else {
                    // Already started before - just resume from current position
                    _commands.emit(ReaderCommand.ResumePlayback)
                    // Note: isPlaying will be updated via UpdatePlayingState when player confirms
                }
            }
        }
    }

    private fun seekTo(audioTimestampMs: Long) {
        viewModelScope.launch {
            _commands.emit(ReaderCommand.SeekToAudioPosition(audioTimestampMs))
            updateState { it.copy(currentAudioPositionMs = audioTimestampMs) }
        }
    }

    private fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            _commands.emit(ReaderCommand.SetPlaybackSpeed(speed))
            updateState { it.copy(playbackSpeed = speed) }
            // Also save the speed to settings
            val currentSettings = viewState.value.publication?.initialSettings
            currentSettings?.let { settings ->
                saveReaderSettingsUseCase(settings.copy(playbackSpeed = speed).toDomainModel())
            }
        }
    }

    /**
     * Skips forward by a fixed increment (10 seconds).
     * Delegates to the player which uses its authoritative position.
     * The milliseconds parameter is ignored - the player uses a fixed 10-second increment.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun skipForward(milliseconds: Long) {
        viewModelScope.launch {
            _commands.emit(ReaderCommand.SkipForward)
        }
    }

    /**
     * Skips backward by a fixed increment (10 seconds).
     * Delegates to the player which uses its authoritative position.
     * The milliseconds parameter is ignored - the player uses a fixed 10-second increment.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun skipBackward(milliseconds: Long) {
        viewModelScope.launch {
            _commands.emit(ReaderCommand.SkipBackward)
        }
    }

    /**
     * Updates the current audio position from the navigator.
     * Called by the View when the navigator reports position changes.
     */
    private fun updateAudioPosition(positionMs: Long, totalDurationMs: Long?) {
        println("čič updateAudioPosition: positionMs=$positionMs, totalDurationMs=$totalDurationMs")
        val currentState = viewState.value
        updateState {
            it.copy(
                // Only update position if playback has started, or if new position is not 0
                // This preserves the saved position before first playback
                currentAudioPositionMs = if (currentState.hasStartedPlayback || positionMs > 0) {
                    positionMs
                } else {
                    it.currentAudioPositionMs
                },
                totalDurationMs = totalDurationMs ?: it.totalDurationMs,
            )
        }
    }

    /**
     * Updates the playing state from the navigator.
     * Called by the View when the navigator reports playback state changes.
     */
    private fun updatePlayingState(isPlaying: Boolean) {
        updateState { it.copy(isPlaying = isPlaying) }
        if (!isPlaying) {
            saveCurrentAudioPosition()
        }
    }

    /**
     * Saves the current audio position to persistence.
     * This is called when playback is paused or the reader is closed.
     */
    private fun saveCurrentAudioPosition() {
        val currentState = viewState.value
        val audioPositionMs = currentState.currentAudioPositionMs
        val lastPosition = currentState.lastKnownPosition

        println("čič saveCurrentAudioPosition: audioPositionMs=$audioPositionMs, lastPosition=${lastPosition != null}")

        if (audioPositionMs <= 0) return
        if (lastPosition == null) return

        viewModelScope.launch {
            // Create a position with the current audio timestamp
            val now = now().toString()
            val positionDomainModel = PositionDomainModel(
                bookUuid = bookUuid,
                timestamp = nowMillis(),
                createdAt = lastPosition.createdAt,
                updatedAt = now,
                locatorHref = lastPosition.href,
                locatorType = lastPosition.type,
                locatorTitle = lastPosition.title,
                locatorTarget = null,
                audioTimestampMs = audioPositionMs,
                chapterIndex = lastPosition.chapterIndex,
                progression = lastPosition.progression,
                totalChapters = lastPosition.totalChapters,
                totalDurationMs = currentState.totalDurationMs,
                totalProgression = lastPosition.totalProgression,
                position = lastPosition.position,
            )
            saveReadingProgressUseCase(positionDomainModel)
        }
    }
}
