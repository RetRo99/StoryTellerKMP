package com.retro99.reader.ui.reader

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.ReaderAnalyticsEvent
import com.retro99.base.formatCurrentTime
import com.retro99.base.now
import com.retro99.base.nowMillis
import com.retro99.base.result.log
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.model.ChapterProgressDisplayMode
import com.retro99.reader.domain.model.CurrentlyReadingDomainModel
import com.retro99.reader.domain.model.PositionDomainModel
import com.retro99.reader.domain.model.ReaderInitializationData
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.InitializeReaderUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReadingProgressUseCase
import com.retro99.reader.domain.usecase.SetCurrentlyReadingUseCase
import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReadAloudHighlightColor
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.toDomainModel
import com.retro99.reader.ui.model.toPositionUiModel
import com.retro99.reader.ui.model.toUiData
import com.retro99.reader.ui.model.toUiModel
import com.retro99.reader.ui.navigator.AudioController
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.service.EpubPublicationService
import com.retro99.statistics.domain.usecase.SaveReadingSessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform.getKoin

@KoinViewModel
class ReaderViewModel(
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val bookType: BookType,
    @InjectedParam private val onClose: () -> Unit,
    @InjectedParam private val onSettingsClick: () -> Unit,
    @Provided private val initializeReaderUseCase: InitializeReaderUseCase,
    @Provided private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @Provided private val saveReadingSessionUseCase: SaveReadingSessionUseCase,
    @Provided private val setCurrentlyReadingUseCase: SetCurrentlyReadingUseCase,
    @Provided private val publicationService: EpubPublicationService,
    @Provided private val analytics: Analytics,
) : BaseViewModel<ReaderViewState, ReaderIntent>(
    ReaderViewState(
        bookUuid = bookUuid,
        bookType = bookType,
    )
) {

    private val readerScope: Scope by lazy {
        getKoin().createScope<ReaderScope>(bookUuid).apply {
            viewState.value.publication?.let {
                declare(it)
            }
        }
    }

    private val bookController: BookController by lazy {
        readerScope.get<BookController>().also {
            addCloseable(it)
        }
    }

    private val audioController: AudioController by lazy {
        readerScope.get<AudioController>().also {
            addCloseable(it)
        }
    }

    private val syncCoordinator: ReaderSyncCoordinator by lazy {
        readerScope.get<ReaderSyncCoordinator>().also {
            addCloseable(it)
        }
    }

    private val readingSpeedTracker: ReadingSpeedTracker by lazy {
        readerScope.get<ReadingSpeedTracker>()
    }

    /** Job for the time update coroutine, cancelled when showCurrentTime is disabled */
    private var timeUpdateJob: Job? = null

    /** Timestamp when the book was opened, used for calculating reading duration */
    private var bookOpenedTimestamp: Long = 0L

    /** Tracks the previous playing state to detect play/pause transitions */
    private var wasPlaying: Boolean = false

    init {
        initializeReader()
        observeShowCurrentTimeSetting()
    }

    /**
     * Observes the showCurrentTime setting and starts/stops time updates accordingly.
     * When enabled, updates the current time immediately and then every minute.
     * When disabled, cancels the time update coroutine and clears the current time.
     */
    private fun observeShowCurrentTimeSetting() {
        getReaderSettingsUseCase()
            .map { it.showCurrentTime }
            .distinctUntilChanged()
            .onEach { showCurrentTime ->
                if (showCurrentTime) {
                    startTimeUpdates()
                } else {
                    stopTimeUpdates()
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Starts periodic updates of the current time.
     * Updates immediately and then every minute.
     */
    private fun startTimeUpdates() {
        // Cancel any existing job before starting a new one
        timeUpdateJob?.cancel()
        timeUpdateJob = viewModelScope.launch {
            while (true) {
                updateState { it.copy(currentTime = formatCurrentTime()) }
                delay(TIME_UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Stops the periodic time updates and clears the current time display.
     */
    private fun stopTimeUpdates() {
        timeUpdateJob?.cancel()
        timeUpdateJob = null
        updateState { it.copy(currentTime = "") }
    }

    override fun onIntent(intent: ReaderIntent) {
        when (intent) {
            is ReaderIntent.UpdateSettings -> updateSettings(intent.settings)
            ReaderIntent.ToggleSettings -> toggleSettings()
            ReaderIntent.Close -> close()
            ReaderIntent.OnSettingsClicked -> onSettingsClick()
            ReaderIntent.UseLocalPosition -> resolveConflictWithLocal()
            ReaderIntent.UseRemotePosition -> resolveConflictWithRemote()
            ReaderIntent.GoToNextPage -> goToNextPage()
            ReaderIntent.GoToPreviousPage -> goToPreviousPage()
            ReaderIntent.TogglePlayback -> togglePlayback()
            is ReaderIntent.SeekTo -> seekTo(intent.audioTimestampMs)
            is ReaderIntent.SetPlaybackSpeed -> setPlaybackSpeed(intent.speed)
            is ReaderIntent.SkipForward -> skipForward(intent.milliseconds)
            is ReaderIntent.SkipBackward -> skipBackward(intent.milliseconds)
            ReaderIntent.ToggleToc -> toggleToc()
            is ReaderIntent.GoToChapter -> goToChapter(intent.href, intent.currentPosition)
            is ReaderIntent.UndoChapterNavigation -> undoChapterNavigation(intent.position)
            ReaderIntent.DismissChapterNavigationUndo -> dismissChapterNavigationUndo()
            is ReaderIntent.SetHighlightColor -> setHighlightColor(intent.color)
            ReaderIntent.Retry -> retry()
            ReaderIntent.DismissNoAudioMessage -> dismissNoAudioMessage()
        }
    }

    private fun dismissNoAudioMessage() {
        updateState { it.copy(showNoAudioMessage = false) }
    }

    private fun retry() {
        updateState { it.copy(error = null) }
        initializeReader()
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
                val uiSettings = settings.toUiModel()
                bookController.setSettings(uiSettings)
                updateState { it.copy(currentSettings = uiSettings) }
                // Refresh chapter page info after settings change (layout may have changed)
                // Only needed when using RELATIVE display mode (viewport-based page numbers)
                if (uiSettings.chapterProgressDisplayMode == ChapterProgressDisplayMode.RELATIVE) {
                    refreshChapterPageInfo()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeBookLocationChanges() {
        bookController.currentLocator
            .onEach { locator ->
                val currentState = viewState.value
                val positionUiModel = locator.toPositionUiModel(
                    basePosition = currentState.lastKnownPosition,
                    createdAt = now().toString(),
                )
                updatePosition(positionUiModel)

                val currentSettings = currentState.currentSettings
                val needsPageInfoForDisplay =
                    currentSettings?.chapterProgressDisplayMode == ChapterProgressDisplayMode.RELATIVE

                // Fetch chapter page info for RELATIVE display mode
                if (needsPageInfoForDisplay) {
                    val chapterPageInfo = bookController.getChapterPageInfo()
                    updateState { it.copy(chapterPageInfo = chapterPageInfo) }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observes reading time info from the ReadingSpeedTracker.
     * The tracker handles all the logic internally (settings, page info, word count).
     */
    private fun observeReadingTimeInfo() {
        readingSpeedTracker.readingTimeInfo
            .onEach { readingTimeInfo ->
                updateState { it.copy(chapterReadingTimeInfo = readingTimeInfo) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Persists a confident reading speed measurement to settings.
     */
    private fun observeReadingSpeedPersistence() {
        readingSpeedTracker.establishedReadingSpeedWpm
            .filterNotNull()
            .distinctUntilChanged()
            .combine(getReaderSettingsUseCase()) { wpm, settings -> wpm to settings }
            .onEach { (wpm, settings) ->
                if (settings.readingSpeedWpm != wpm) {
                    saveReaderSettingsUseCase(settings.copy(readingSpeedWpm = wpm))
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Refreshes the chapter page info after a delay to allow the WebView to re-layout.
     * This is called after settings changes (font size, margins, etc.) and on initial load.
     */
    private fun refreshChapterPageInfo() {
        viewModelScope.launch {
            // Delay to allow WebView to re-layout after settings change
            delay(CHAPTER_PAGE_INFO_REFRESH_DELAY_MS)
            val chapterPageInfo = bookController.getChapterPageInfo()
            updateState { it.copy(chapterPageInfo = chapterPageInfo) }
        }
    }

    private fun observeAudioPlaybackState() {
        audioController.audioPlaybackState
            .onEach { state ->
                updateAudioPosition(
                    positionMs = state.currentPositionMs,
                    totalDurationMs = state.totalDurationMs,
                )
            }
            .launchIn(viewModelScope)

        audioController.audioPlaybackState
            .map { it.isPlaying }
            .distinctUntilChanged()
            .onEach { isPlaying ->
                updatePlayingState(isPlaying)
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
                    error.log(analytics, "ReaderViewModel: Failed to initialize reader")
                    updateState { it.copy(error = error) }
                }
        }
    }

    private suspend fun openPublication(data: ReaderInitializationData) {
        val settings = data.initialSettings.toUiModel()
        val bookType = data.bookType
        val (position, conflict) = data.progressResult.toUiData()

        publicationService.openPublication(
            filePath = data.localEbookPath,
            bookUuid = data.bookUuid,
            initialSettings = settings,
            bookType = bookType,
            initialPosition = position,
        ).onSuccess { publication ->
            // Track book opened event
            bookOpenedTimestamp = nowMillis()
            analytics.logEvent(
                ReaderAnalyticsEvent.BookOpened(
                    bookUuid = data.bookUuid,
                    bookType = bookType.name,
                )
            )

            updateState { state ->
                state.copy(
                    bookUuid = data.bookUuid,
                    bookTitle = data.bookTitle,
                    bookCoverUrl = data.bookCoverUrl,
                    publication = publication,
                    bookType = bookType,
                    positionConflict = conflict,
                    playbackSpeed = settings.playbackSpeed,
                    error = null,
                    currentAudioPositionMs = position?.audioTimestampMs ?: 0L,
                    lastKnownPosition = position,
                    tableOfContents = publication.tableOfContents,
                )
            }
            // Start observing after publication is ready
            observeBookLocationChanges()
            observeReadingTimeInfo()
            observeReadingSpeedPersistence()
            observeSettingsChanges()
            // Fetch initial chapter page info after WebView renders
            // Only needed when using RELATIVE display mode (viewport-based page numbers)
            if (settings.chapterProgressDisplayMode == ChapterProgressDisplayMode.RELATIVE) {
                refreshChapterPageInfo()
            }
            // Initialize audio after publication is in state
            if (publication.hasMediaOverlays) {
                initAudio()
            } else if (bookType == BookType.READALOUD) {
                // Track when a ReadAloud book is missing media overlays and show snackbar
                analytics.logEvent(
                    ReaderAnalyticsEvent.ReadAloudMissingMediaOverlays(bookUuid = data.bookUuid)
                )
                updateState { it.copy(showNoAudioMessage = true) }
            }
        }.onFailure { error ->
            analytics.logEvent(
                ReaderAnalyticsEvent.BookOpenFailed(
                    bookUuid = data.bookUuid,
                    bookType = bookType.name,
                    errorMessage = error.message ?: "Unknown publication error",
                )
            )
            updateState { it.copy(error = error) }
        }
    }

    private fun initAudio() {
        // Start sync coordinator (this also triggers lazy initialization of audioController)
        // Note: Initial audio position is handled via constructor injection in AudioController
        syncCoordinator.start(viewModelScope)

        audioController.audioPlaybackState
            .map { it.isPlayerReady }
            .distinctUntilChanged()
            .filter { it }
            .onEach {
                updateState { it.copy(isAudioPlayerReady = true) }
            }
            .launchIn(viewModelScope)
        observeAudioPlaybackState()
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
        val currentState = viewState.value
        viewModelScope.launch {
            updateState {
                it.copy(
                    positionConflict = null,
                    currentAudioPositionMs = conflict.remotePosition.audioTimestampMs ?: 0L,
                )
            }
            bookController.goToPosition(conflict.remotePosition)
            // Also update the audio position if this is a ReadAloud book with actual media overlays
            if (currentState.isReadAloud) {
                audioController.setInitialAudioPosition(conflict.remotePosition.audioTimestampMs)
            }
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
        val willBeVisible = !viewState.value.isSettingsVisible
        if (willBeVisible) {
            analytics.logEvent(ReaderAnalyticsEvent.SettingsOpened(bookUuid = bookUuid))
        }
        updateState { it.copy(isSettingsVisible = willBeVisible) }
    }

    private fun toggleToc() {
        val willBeVisible = !viewState.value.isTocVisible
        if (willBeVisible) {
            analytics.logEvent(ReaderAnalyticsEvent.TocOpened(bookUuid = bookUuid))
        }
        updateState { it.copy(isTocVisible = willBeVisible) }
    }

    private fun goToChapter(href: String, currentPosition: PositionUiModel?) {
        bookController.goToChapter(href)
        updateState {
            it.copy(
                isTocVisible = false,
                previousTocPosition = currentPosition,
            )
        }
    }

    private fun undoChapterNavigation(position: PositionUiModel) {
        bookController.goToPosition(position)
        updateState { it.copy(previousTocPosition = null) }
    }

    private fun dismissChapterNavigationUndo() {
        updateState { it.copy(previousTocPosition = null) }
    }

    fun close() {
        viewModelScope.launch {
            // Only save audio position if this is a ReadAloud book with actual media overlays
            if (viewState.value.isReadAloud) {
                saveCurrentAudioPositionSync()
            }

            // Track book closed event with reading duration and progress
            val currentState = viewState.value
            val endTime = nowMillis()
            val readingDurationMs = if (bookOpenedTimestamp > 0) {
                endTime - bookOpenedTimestamp
            } else {
                0L
            }
            val progressPercent = currentState.lastKnownPosition?.totalProgression
                ?.let { (it * 100).toInt() } ?: 0
            val sessionReadingSpeedWpm = readingSpeedTracker.establishedReadingSpeedWpm.value
                ?: currentState.currentSettings?.readingSpeedWpm
                ?: 250

            analytics.logEvent(
                ReaderAnalyticsEvent.BookClosed(
                    bookUuid = bookUuid,
                    readingDurationMs = readingDurationMs,
                    progressPercent = progressPercent,
                )
            )

            // Save reading session for statistics (only if we have a valid session)
            if (bookOpenedTimestamp > 0 && readingDurationMs > 0 && currentState.bookTitle.isNotEmpty()) {
                saveReadingSessionUseCase(
                    bookUuid = bookUuid,
                    bookTitle = currentState.bookTitle,
                    bookType = currentState.bookType,
                    startTime = bookOpenedTimestamp,
                    endTime = endTime,
                    durationMs = readingDurationMs,
                    endProgression = currentState.lastKnownPosition?.totalProgression,
                    readingSpeedWpm = sessionReadingSpeedWpm,
                )
            }

            // Update currently reading book if session was long enough (≥ 1 minute)
            if (readingDurationMs >= MINIMUM_READING_DURATION_MS && currentState.bookTitle.isNotEmpty()) {
                setCurrentlyReadingUseCase(
                    CurrentlyReadingDomainModel(
                        bookUuid = bookUuid,
                        bookType = currentState.bookType,
                        bookTitle = currentState.bookTitle,
                        coverUrl = currentState.bookCoverUrl,
                        totalProgression = currentState.lastKnownPosition?.totalProgression,
                    )
                )
            }

            onClose()
        }
    }

    /**
     * Toggles audio playback.
     *
     * The AudioController handles all the logic internally:
     * - Whether to start fresh (with positioning) or resume
     * - Permission checks, audio focus, foreground service
     * - Uses the visible sentence set by ReaderSyncCoordinator for precise positioning
     *
     * The UI will update when the player reports its actual state.
     */
    private fun togglePlayback() {
        audioController.togglePlayback()
    }

    private fun seekTo(audioTimestampMs: Long) {
        audioController.seekToAudioPosition(audioTimestampMs)
        updateState { it.copy(currentAudioPositionMs = audioTimestampMs) }
    }

    private fun setPlaybackSpeed(speed: Float) {
        analytics.logEvent(
            ReaderAnalyticsEvent.SettingChanged("playback_speed", speed.toString())
        )
        audioController.setPlaybackSpeed(speed)
        updateState { it.copy(playbackSpeed = speed) }
        // Also save the speed to settings
        val currentSettings = viewState.value.publication?.initialSettings
        viewModelScope.launch {
            currentSettings?.let { settings ->
                saveReaderSettingsUseCase(settings.copy(playbackSpeed = speed).toDomainModel())
            }
        }
    }

    private fun setHighlightColor(color: ReadAloudHighlightColor) {
        val currentSettings = viewState.value.currentSettings ?: return
        val updatedSettings = currentSettings.copy(highlightColor = color)
        updateState { it.copy(currentSettings = updatedSettings) }
        viewModelScope.launch {
            saveReaderSettingsUseCase(updatedSettings.toDomainModel())
        }
    }

    /**
     * Skips forward by a fixed increment (10 seconds).
     * Delegates to the player which uses its authoritative position.
     * The milliseconds parameter is ignored - the player uses a fixed 10-second increment.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun skipForward(milliseconds: Long) {
        audioController.skipForward()
    }

    /**
     * Skips backward by a fixed increment (10 seconds).
     * Delegates to the player which uses its authoritative position.
     * The milliseconds parameter is ignored - the player uses a fixed 10-second increment.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun skipBackward(milliseconds: Long) {
        audioController.skipBackward()
    }

    /**
     * Updates the current audio position from the navigator.
     * Called by the View when the navigator reports position changes.
     * Position is only updated if positionMs is not null (null means position not yet known).
     * Duration is always updated if available.
     */
    private fun updateAudioPosition(positionMs: Long?, totalDurationMs: Long?) {
        updateState {
            it.copy(
                currentAudioPositionMs = positionMs ?: it.currentAudioPositionMs,
                totalDurationMs = totalDurationMs ?: it.totalDurationMs,
            )
        }
    }

    /**
     * Updates the playing state from the navigator.
     * Called when the audio controller reports playback state changes.
     */
    private fun updatePlayingState(isPlaying: Boolean) {
        // Track when playback starts (feature usage)
        if (isPlaying && !wasPlaying) {
            analytics.logEvent(ReaderAnalyticsEvent.PlaybackStarted(bookUuid = bookUuid))
        }
        wasPlaying = isPlaying

        updateState { it.copy(isPlaying = isPlaying) }
        if (!isPlaying) {
            saveCurrentAudioPosition()
        }
    }

    /**
     * Saves the current audio position to persistence.
     * This is called when playback is paused.
     */
    private fun saveCurrentAudioPosition() {
        viewModelScope.launch {
            saveCurrentAudioPositionSync()
        }
    }

    /**
     * Saves the current audio position to persistence synchronously.
     * This is called when the reader is closed to ensure the position is saved
     * before navigation occurs.
     */
    private suspend fun saveCurrentAudioPositionSync() {
        val currentState = viewState.value
        val audioPositionMs = currentState.currentAudioPositionMs
        val lastPosition = currentState.lastKnownPosition

        if (audioPositionMs <= 0) return
        if (lastPosition == null) return

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

    override fun onCleared() {
        super.onCleared()
        readerScope.close()
    }

    private companion object {
        /** Delay before refreshing chapter page info to allow WebView to re-layout */
        private const val CHAPTER_PAGE_INFO_REFRESH_DELAY_MS = 300L

        /** Interval for updating the current time display (1 minute) */
        private const val TIME_UPDATE_INTERVAL_MS = 60_000L

        /** Minimum reading duration to update "currently reading" book (1 minute) */
        private const val MINIMUM_READING_DURATION_MS = 60_000L
    }
}
