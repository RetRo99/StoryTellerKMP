package com.retro99.reader.ui.reader

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.result.AppError
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.model.PositionDomainModel
import com.retro99.books.domain.usecase.GetBookByUuidUseCase
import com.retro99.books.ui.model.PositionUiModel
import com.retro99.books.ui.model.toUiModel
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.GetReadingProgressUseCase
import com.retro99.reader.domain.usecase.PrepareEbookUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReadingProgressUseCase
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.toDomainModel
import com.retro99.reader.ui.model.toUiModel
import com.retro99.reader.ui.service.EpubPublicationService
import kotlinx.coroutines.async
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
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val onClose: () -> Unit,
    @Provided private val getBookByUuidUseCase: GetBookByUuidUseCase,
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
        loadBook()
        observeSettingsChanges()
    }

    override fun onIntent(intent: ReaderIntent) {
        when (intent) {
            is ReaderIntent.UpdatePosition -> updatePosition(intent.position)
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

    private fun loadBook() {
        viewModelScope.launch {
            getBookByUuidUseCase(bookUuid)
                .first()
                .onSuccess { book ->
                    val ebookFilepath = book.ebook?.filepath
                    if (ebookFilepath == null) {
                        updateState {
                            it.copy(error = AppError.UnknownError(Throwable("Book has no ebook")))
                        }
                        return@onSuccess
                    }
                    prepareAndOpenPublication(bookUuid, ebookFilepath)
                }
                .onFailure { error ->
                    updateState { it.copy(error = error) }
                }
        }
    }

    private suspend fun prepareAndOpenPublication(uuid: String, ebookFilepath: String) {
        prepareEbookUseCase(uuid, ebookFilepath)
            .fold(
                success = { localPath ->
                    openPublication(uuid, localPath)
                },
                failure = { error ->
                    updateState { it.copy(error = error) }
                },
            )
    }

    private suspend fun openPublication(uuid: String, localPath: String) {
        val settingsDeferred = viewModelScope.async {
            getReaderSettingsUseCase().first().toUiModel()
        }
        val positionDeferred = viewModelScope.async {
            getReadingProgressUseCase(uuid).fold(
                success = { it?.toUiModel() },
                failure = { null },
            )
        }

        val initialSettings = settingsDeferred.await()
        val initialPosition = positionDeferred.await()

        val publication =
            publicationService.openPublication(localPath, initialSettings, initialPosition)
        if (publication != null) {
            updateState {
                it.copy(
                    bookUuid = uuid,
                    publication = publication,
                    position = initialPosition,
                    error = null,
                )
            }
        } else {
            updateState { it.copy(error = AppError.UnknownError(Throwable("Failed to open publication"))) }
        }
    }

    private fun updatePosition(position: PositionUiModel) {
        updateState { it.copy(position = position) }
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

