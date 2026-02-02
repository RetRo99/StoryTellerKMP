package com.retro99.reader.ui.reader

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.retro99.base.result.AppError
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.ui.model.BookUiModel
import com.retro99.reader.domain.model.InitialLocatorDomainModel
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.GetReadingProgressUseCase
import com.retro99.reader.domain.usecase.PrepareEbookUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReadingProgressUseCase
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.ReadingProgressUiModel
import com.retro99.reader.ui.model.toDomainModel
import com.retro99.reader.ui.model.toUiModel
import com.retro99.reader.ui.service.EpubPublicationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import kotlin.time.Clock

@KoinViewModel
class ReaderViewModel(
    @InjectedParam private val book: BookUiModel,
    @InjectedParam private val onClose: () -> Unit,
    @Provided private val prepareEbookUseCase: PrepareEbookUseCase,
    @Provided private val getReadingProgressUseCase: GetReadingProgressUseCase,
    @Provided private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @Provided private val publicationService: EpubPublicationService,
) : BaseViewModel<ReaderViewState, ReaderIntent>(ReaderViewState()) {

    private val bookUuid: String = book.uuid
    private val ebookFilePath: String = book.ebookFilepath ?: ""

    private val initialLocator: InitialLocatorDomainModel? =
        book.locator?.let { locator ->
            val href = locator.href ?: return@let null
            val type = locator.type ?: return@let null
            InitialLocatorDomainModel(
                href = href,
                type = type,
                progression = locator.progression,
                position = locator.position,
                totalProgression = locator.totalProgression,
            )
        }

    private val _commands = MutableSharedFlow<ReaderCommand>()
    val commands: SharedFlow<ReaderCommand> = _commands.asSharedFlow()

    init {
        loadBook()
        observeSettingsChanges()
    }

    override fun onIntent(intent: ReaderIntent) {
        when (intent) {
            is ReaderIntent.UpdateProgress -> updateProgress(intent.locator, intent.progression)
            is ReaderIntent.UpdateSettings -> updateSettings(intent.settings)
            ReaderIntent.ToggleSettings -> toggleSettings()
            ReaderIntent.Close -> close()
        }
    }

    private fun observeSettingsChanges() {
        getReaderSettingsUseCase()
            .onEach { settings ->
                _commands.emit(ReaderCommand.ApplySettings(settings.toUiModel()))
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
        // Get initial settings synchronously before opening publication
        val initialSettings = getReaderSettingsUseCase().first().toUiModel()

        val publication =
            publicationService.openPublication(localPath, initialSettings, initialLocator)
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
                        updateState { it.copy(progress = progress?.toUiModel()) }
                    },
                    failure = { /* Ignore progress loading failure */ },
                )
        }
    }

    private fun updateProgress(locator: String, progression: Float) {
        val currentBookUuid = currentViewState().bookUuid ?: return

        val progressUiModel = ReadingProgressUiModel(
            bookUuid = currentBookUuid,
            locator = locator,
            progression = progression,
            lastReadAt = Clock.System.now().toString(),
        )

        updateState { it.copy(progress = progressUiModel) }

        viewModelScope.launch {
            saveReadingProgressUseCase(progressUiModel.toDomainModel())
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

