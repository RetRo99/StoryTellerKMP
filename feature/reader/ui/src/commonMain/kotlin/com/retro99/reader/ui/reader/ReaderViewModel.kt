package com.retro99.reader.ui.reader

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.result.AppError
import com.retro99.base.ui.BaseViewModel
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
import kotlin.time.Clock

@KoinViewModel
class ReaderViewModel(
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val onClose: () -> Unit,
    @Provided private val initializeReaderUseCase: InitializeReaderUseCase,
    @Provided private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @Provided private val publicationService: EpubPublicationService,
) : BaseViewModel<ReaderViewState, ReaderIntent>(ReaderViewState()) {

    private val _commands = MutableSharedFlow<ReaderCommand>()
    val commands: SharedFlow<ReaderCommand> = _commands.asSharedFlow()

    init {
        initializeReader()
        observeSettingsChanges()
    }

    override fun onIntent(intent: ReaderIntent) {
        when (intent) {
            is ReaderIntent.UpdatePosition -> updatePosition(intent.position)
            is ReaderIntent.UpdateSettings -> updateSettings(intent.settings)
            ReaderIntent.ToggleSettings -> toggleSettings()
            ReaderIntent.Close -> close()
            ReaderIntent.UseLocalPosition -> resolveConflictWithLocal()
            ReaderIntent.UseRemotePosition -> resolveConflictWithRemote()
        }
    }

    private fun observeSettingsChanges() {
        getReaderSettingsUseCase()
            .onEach { settings ->
                _commands.emit(ReaderCommand.ApplySettings(settings.toUiModel()))
            }
            .launchIn(viewModelScope)
    }

    private fun initializeReader() {
        viewModelScope.launch {
            initializeReaderUseCase(bookUuid)
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
        val (position, conflict) = data.progressResult.toUiData()

        val publication = publicationService.openPublication(
            filePath = data.localEbookPath,
            initialSettings = settings,
            initialPosition = position,
        )

        publication?.let {
            updateState { state ->
                state.copy(
                    bookUuid = data.bookUuid,
                    publication = it,
                    positionConflict = conflict,
                    error = null,
                )
            }
        }
            ?: updateState { it.copy(error = AppError.UnknownError(Throwable("Failed to open publication"))) }
    }

    private fun resolveConflictWithLocal() {
        val conflict = viewState.value.positionConflict ?: return
        viewModelScope.launch {
            updateState { it.copy(positionConflict = null) }
            _commands.emit(ReaderCommand.GoToPosition(conflict.localPosition))
        }
    }

    private fun resolveConflictWithRemote() {
        val conflict = viewState.value.positionConflict ?: return
        viewModelScope.launch {
            updateState { it.copy(positionConflict = null) }
            _commands.emit(ReaderCommand.GoToPosition(conflict.remotePosition))
        }
    }

    private fun updatePosition(position: PositionUiModel) {
        if (viewState.value.positionConflict != null) return

        val now = Clock.System.now().toString()
        val positionDomainModel = PositionDomainModel(
            bookUuid = bookUuid,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            createdAt = position.createdAt,
            updatedAt = now,
            locatorHref = position.href,
            locatorType = position.type,
            locatorTitle = position.title,
            locatorTarget = null,
            audioTimestampMs = null,
            chapterIndex = position.chapterIndex,
            progression = position.progression,
            totalChapters = position.totalChapters,
            totalDurationMs = null,
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
        onClose()
    }
}
