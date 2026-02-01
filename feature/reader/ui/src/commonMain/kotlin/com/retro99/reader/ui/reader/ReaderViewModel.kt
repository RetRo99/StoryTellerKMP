package com.retro99.reader.ui.reader

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.retro99.base.result.AppError
import com.retro99.base.ui.BaseViewModel
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReadingProgressDomainModel
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.GetReadingProgressUseCase
import com.retro99.reader.domain.usecase.PrepareEbookUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReadingProgressUseCase
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
    @InjectedParam private val ebookFilePath: String,
    @InjectedParam private val onClose: () -> Unit,
    @Provided private val prepareEbookUseCase: PrepareEbookUseCase,
    @Provided private val getReadingProgressUseCase: GetReadingProgressUseCase,
    @Provided private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @Provided private val publicationService: EpubPublicationService,
) : BaseViewModel<ReaderViewState, ReaderIntent>(ReaderViewState()) {

    private val _commands = MutableSharedFlow<ReaderCommand>()
    val commands: SharedFlow<ReaderCommand> = _commands.asSharedFlow()

    init {
        observeSettings()
        loadBook()
    }

    override fun onIntent(intent: ReaderIntent) {
        when (intent) {
            is ReaderIntent.UpdateProgress -> updateProgress(intent.locator, intent.progression)
            is ReaderIntent.UpdateSettings -> updateSettings(intent.settings)
            ReaderIntent.ToggleSettings -> toggleSettings()
            ReaderIntent.Close -> close()
        }
    }

    private fun observeSettings() {
        getReaderSettingsUseCase()
            .onEach { settings ->
                updateState { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadBook(uuid: String = bookUuid, filePath: String = ebookFilePath) {
        viewModelScope.launch {
            prepareEbookUseCase(uuid, filePath)
                .fold(
                    success = { localPath ->
                        openPublication(uuid, localPath)
                    },
                    failure = { error ->
                        updateState { it.copy(error = error) }
                    },
                )
        }
    }

    private suspend fun openPublication(uuid: String, localPath: String) {
        val publication = publicationService.openPublication(localPath)
        if (publication != null) {
            updateState {
                it.copy(
                    bookUuid = uuid,
                    publication = publication,
                    error = null,
                )
            }
            loadReadingProgress(uuid)
        } else {
            updateState { it.copy(error = AppError.UnknownError(Throwable("Failed to open publication"))) }
        }
    }

    private fun loadReadingProgress(uuid: String) {
        viewModelScope.launch {
            getReadingProgressUseCase(uuid)
                .fold(
                    success = { progress ->
                        updateState { it.copy(progress = progress) }
                    },
                    failure = { /* Ignore progress loading failure */ },
                )
        }
    }

    private fun updateProgress(locator: String, progression: Float) {
        val currentBookUuid = currentViewState().bookUuid ?: return

        val progressModel = ReadingProgressDomainModel(
            bookUuid = currentBookUuid,
            locator = locator,
            progression = progression,
            lastReadAt = Clock.System.now().toString(),
        )

        updateState { it.copy(progress = progressModel) }

        viewModelScope.launch {
            saveReadingProgressUseCase(progressModel)
        }
    }

    private fun updateSettings(settings: ReaderSettingsDomainModel) {
        viewModelScope.launch {
            saveReaderSettingsUseCase(settings)
        }
    }

    private fun toggleSettings() {
        updateState { it.copy(isSettingsVisible = !it.isSettingsVisible) }
    }

    private fun close() {
        onClose()
    }
}

