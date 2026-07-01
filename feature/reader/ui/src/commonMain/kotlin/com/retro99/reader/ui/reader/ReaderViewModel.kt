package com.retro99.reader.ui.reader

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
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
import com.retro99.reader.domain.model.BookmarkDomainModel
import com.retro99.reader.domain.model.ReaderInitializationData
import com.retro99.reader.domain.usecase.GetCustomReaderFontsUseCase
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.InitializeReaderUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReadingProgressUseCase
import com.retro99.reader.domain.usecase.SetCurrentlyReadingUseCase
import com.retro99.reader.domain.usecase.AddBookmarkUseCase
import com.retro99.reader.domain.usecase.ObserveBookmarksUseCase
import com.retro99.reader.domain.usecase.DeleteBookmarkUseCase
import com.retro99.reader.domain.usecase.UpdateBookmarkTitleUseCase
import com.retro99.reader.domain.usecase.ReorderBookmarksUseCase
import com.retro99.reader.ui.di.InitialAudioPosition
import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.BookmarkUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.toDomainModel
import com.retro99.reader.ui.model.toPositionUiModel
import com.retro99.reader.ui.model.toUiData
import com.retro99.reader.ui.model.toUiModel
import com.retro99.reader.ui.navigator.AudioController
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.publication.PublicationState
import com.retro99.reader.ui.service.EpubPublicationService
import com.retro99.statistics.domain.usecase.SaveReadingSessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.TimeSource
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform.getKoin

@KoinViewModel
class ReaderViewModel(
    @InjectedParam private val serverId: String,
    @InjectedParam private val bookUuid: String,
    @InjectedParam private val bookType: BookType,
    @InjectedParam private val onClose: () -> Unit,
    @InjectedParam private val onSettingsClick: () -> Unit,
    @Provided private val initializeReaderUseCase: InitializeReaderUseCase,
    @Provided private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val getCustomReaderFontsUseCase: GetCustomReaderFontsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @Provided private val saveReadingSessionUseCase: SaveReadingSessionUseCase,
    @Provided private val setCurrentlyReadingUseCase: SetCurrentlyReadingUseCase,
    @Provided private val addBookmarkUseCase: AddBookmarkUseCase,
    @Provided private val observeBookmarksUseCase: ObserveBookmarksUseCase,
    @Provided private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    @Provided private val updateBookmarkTitleUseCase: UpdateBookmarkTitleUseCase,
    @Provided private val reorderBookmarksUseCase: ReorderBookmarksUseCase,
    @Provided private val publicationService: EpubPublicationService,
    @Provided private val analytics: Analytics,
) : BaseViewModel<ReaderViewState, ReaderIntent>(
    ReaderViewState(
        bookUuid = bookUuid,
        bookType = bookType,
    )
) {

    private val readerScope: Scope by lazy {
        getKoin().getOrCreateScope<ReaderScope>(bookUuid).apply {
            viewState.value.publicationState?.let { pubState ->
                val initialPositionMs = pubState.position?.audioTimestampMs
                val initialHref = pubState.position?.href
                declare(pubState.publication)
                declare(
                    InitialAudioPosition(
                        positionMs = initialPositionMs,
                        href = initialHref,
                    )
                )
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

    /** Job for the Read Aloud sleep timer countdown. */
    private var sleepTimerJob: Job? = null

    /** Timestamp when the book was opened, used for calculating reading duration */
    private var bookOpenedTimestamp: Long = 0L

    /** Monotonic mark used to generate unique bookmark IDs with nanosecond precision. */
    private val bookmarkIdMark = TimeSource.Monotonic.markNow()

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
            ReaderIntent.ToggleAudioOnlyMode -> toggleAudioOnlyMode()
            is ReaderIntent.SeekTo -> seekTo(intent.audioTimestampMs)
            is ReaderIntent.SetPlaybackSpeed -> setPlaybackSpeed(intent.speed)
            is ReaderIntent.StartSleepTimer -> startSleepTimer(intent.durationMs)
            ReaderIntent.CancelSleepTimer -> cancelSleepTimer()
            ReaderIntent.DismissSleepTimerWarning -> dismissSleepTimerWarning()
            is ReaderIntent.SkipForward -> skipForward(intent.milliseconds)
            is ReaderIntent.SkipBackward -> skipBackward(intent.milliseconds)
            ReaderIntent.ToggleToc -> toggleToc()
            is ReaderIntent.GoToChapter -> goToChapter(intent.href, intent.currentPosition)
            ReaderIntent.GoToNextChapter -> goToNextChapter()
            ReaderIntent.GoToPreviousChapter -> goToPreviousChapter()
            is ReaderIntent.GoToChapterAndPlay -> goToChapterAndPlay(intent.href, intent.currentPosition)
            ReaderIntent.GoToNextChapterAndPlay -> goToNextChapterAndPlay()
            ReaderIntent.GoToPreviousChapterAndPlay -> goToPreviousChapterAndPlay()
            is ReaderIntent.UndoChapterNavigation -> undoChapterNavigation(intent.position)
            ReaderIntent.DismissChapterNavigationUndo -> dismissChapterNavigationUndo()
            is ReaderIntent.SetHighlightColor -> setHighlightColor(intent.colorArgb)
            ReaderIntent.Retry -> retry()
            ReaderIntent.DismissNoAudioMessage -> dismissNoAudioMessage()
            ReaderIntent.DismissBookmarkSaveFailed -> dismissBookmarkSaveFailed()
            ReaderIntent.ToggleBookmarks -> toggleBookmarks()
            ReaderIntent.AddBookmark -> addBookmark()
            ReaderIntent.DismissBookmarkAdded -> dismissBookmarkAdded()
            ReaderIntent.DismissBookmarkAlreadyExists -> dismissBookmarkAlreadyExists()
            is ReaderIntent.UndoBookmark -> undoBookmark(intent.id)
            is ReaderIntent.RenameBookmark -> renameBookmark(intent.id, intent.newTitle)
            is ReaderIntent.ReorderBookmarks -> reorderBookmarks(intent.bookmarkIds)
            ReaderIntent.GoToPreviousBookmark -> goToPreviousBookmark()
            ReaderIntent.GoToNextBookmark -> goToNextBookmark()
            ReaderIntent.DismissNoMoreBookmarks -> dismissNoMoreBookmarks()
            is ReaderIntent.DeleteBookmark -> deleteBookmark(intent.id)
            is ReaderIntent.GoToBookmark -> goToBookmark(intent.bookmark)
        }
    }

    private fun dismissNoAudioMessage() {
        updateState { it.copy(showNoAudioMessage = false) }
    }

    private fun dismissBookmarkSaveFailed() {
        updateState { it.copy(showBookmarkSaveFailed = false) }
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
                updatePublicationState { it.copy(settings = uiSettings) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Updates the PublicationState within the ViewState.
     * Only applies the update if publicationState is not null.
     */
    private inline fun updatePublicationState(crossinline update: (PublicationState) -> PublicationState) {
        updateState { state ->
            state.publicationState?.let { pubState ->
                state.copy(publicationState = update(pubState))
            } ?: state
        }
    }

    private fun observeBookLocationChanges() {
        bookController.currentLocator
            .onEach { locator ->
                val currentState = viewState.value
                val positionUiModel = locator.toPositionUiModel(
                    basePosition = currentState.currentPosition,
                    createdAt = now().toString(),
                )
                updatePosition(positionUiModel)

                // Update chapter info from the enriched locator state
                // (word count is used internally by ReadingSpeedTracker via the locator flow)
                updateState { it.copy(chapterInfo = locator.chapterInfo) }
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

        audioController.currentAudioLocator
            .onEach { audioLocator ->
                val audioHref = audioLocator?.locator?.href ?: return@onEach
                val currentHref = viewState.value.currentPosition?.href
                if (audioHref != currentHref) {
                    updatePublicationState { pubState ->
                        pubState.copy(
                            position = pubState.position?.copy(href = audioHref),
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun initializeReader() {
        viewModelScope.launch {
            initializeReaderUseCase(serverId, bookUuid, bookType)
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
        val customFonts = getCustomReaderFontsUseCase().first()
        val bookType = data.bookType
        val (position, conflict) = data.progressResult.toUiData()

        publicationService.openPublication(
            filePath = data.localEbookPath,
            serverId = data.serverId,
            bookUuid = data.bookUuid,
            bookType = bookType,
        ).onSuccess { publication ->
            // Track book opened event
            bookOpenedTimestamp = nowMillis()
            analytics.logEvent(
                ReaderAnalyticsEvent.BookOpened(
                    bookUuid = data.bookUuid,
                    bookType = bookType.name,
                )
            )

            // Create PublicationState with initial settings and position
            val publicationState = PublicationState(
                publication = publication,
                settings = settings,
                position = position,
                customFonts = customFonts,
            )

            updateState { state ->
                state.copy(
                    bookUuid = data.bookUuid,
                    bookTitle = data.bookTitle,
                    bookCoverUrl = data.bookCoverUrl,
                    publicationState = publicationState,
                    bookType = bookType,
                    positionConflict = conflict,
                    error = null,
                    currentAudioPositionMs = position?.audioTimestampMs ?: 0L,
                    tableOfContents = publication.tableOfContents,
                )
            }
            // Start observing after publication is ready
            observeBookLocationChanges()
            observeReadingTimeInfo()
            observeReadingSpeedPersistence()
            observeSettingsChanges()
            observeCustomFontChanges()
            observeBookmarks()
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

    private fun observeCustomFontChanges() {
        getCustomReaderFontsUseCase()
            .onEach { customFonts ->
                updatePublicationState { it.copy(customFonts = customFonts) }
            }
            .launchIn(viewModelScope)
    }

    private fun initAudio() {
        // Start sync coordinator (this also triggers lazy initialization of audioController)
        // Note: Initial audio position is handled via constructor injection in AudioController
        syncCoordinator.start(viewModelScope)

        // Set now-playing info for mini-player display
        val state = viewState.value
        audioController.setNowPlayingInfo(
            bookUuid = state.bookUuid,
            bookTitle = state.bookTitle,
            coverUrl = state.bookCoverUrl,
        )

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

        updatePublicationState { it.copy(position = position) }

        val now = now().toString()
        val currentState = viewState.value
        val audioTimestamp = currentState.currentAudioPositionMs.takeIf { it > 0 }
        val positionDomainModel = PositionDomainModel(
            bookUuid = bookUuid,
            serverId = serverId,
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

    private fun goToNextChapter() {
        val state = viewState.value
        val chapters = state.tableOfContents
        val currentHref = state.currentPosition?.href
        val currentIndex = chapters.indexOfFirst { it.href == currentHref }
        val nextChapter = chapters.getOrNull(currentIndex + 1) ?: return
        goToChapter(nextChapter.href, state.currentPosition)
    }

    private fun goToPreviousChapter() {
        val state = viewState.value
        val chapters = state.tableOfContents
        val currentHref = state.currentPosition?.href
        val currentIndex = chapters.indexOfFirst { it.href == currentHref }
        val previousChapter = chapters.getOrNull(currentIndex - 1) ?: return
        goToChapter(previousChapter.href, state.currentPosition)
    }

    private fun goToChapterAndPlay(href: String, currentPosition: PositionUiModel?) {
        goToChapter(href, currentPosition)
        updatePublicationState { pubState ->
            pubState.copy(
                position = pubState.position?.copy(href = href, progression = 0.0),
            )
        }
        audioController.playFromFragment(fragmentId = "", chapterHref = href)
    }

    private fun goToNextChapterAndPlay() {
        val state = viewState.value
        val chapters = state.tableOfContents
        val currentHref = state.currentPosition?.href
        val currentIndex = chapters.indexOfFirst { it.href == currentHref }
        val nextChapter = chapters.getOrNull(currentIndex + 1) ?: return
        goToChapterAndPlay(nextChapter.href, state.currentPosition)
    }

    private fun goToPreviousChapterAndPlay() {
        val state = viewState.value
        val chapters = state.tableOfContents
        val currentHref = state.currentPosition?.href
        val currentIndex = chapters.indexOfFirst { it.href == currentHref }
        val previousChapter = chapters.getOrNull(currentIndex - 1) ?: return
        goToChapterAndPlay(previousChapter.href, state.currentPosition)
    }

    private fun undoChapterNavigation(position: PositionUiModel) {
        bookController.goToPosition(position)
        updateState { it.copy(previousTocPosition = null) }
    }

    private fun dismissChapterNavigationUndo() {
        updateState { it.copy(previousTocPosition = null) }
    }

    private fun toggleBookmarks() {
        val willBeVisible = !viewState.value.isBookmarksVisible
        if (willBeVisible) {
            analytics.logEvent(ReaderAnalyticsEvent.BookmarksOpened(bookUuid = bookUuid))
        }
        updateState { it.copy(isBookmarksVisible = willBeVisible) }
    }

    private fun addBookmark() {
        val currentPosition = viewState.value.currentPosition ?: return
        val existing = viewState.value.bookmarks.find { bookmark ->
            bookmark.locatorHref == currentPosition.href &&
                bookmark.position == currentPosition.position
        }
        if (existing != null) {
            updateState {
                it.copy(
                    isBookmarksVisible = false,
                    showBookmarkAlreadyExists = true,
                )
            }
            return
        }
        val nanoSuffix = bookmarkIdMark.elapsedNow().inWholeNanoseconds
        val bookmark = BookmarkDomainModel(
            id = "${bookUuid}_${nowMillis()}_$nanoSuffix",
            bookUuid = bookUuid,
            locatorHref = currentPosition.href,
            locatorType = currentPosition.type,
            locatorTitle = currentPosition.title,
            progression = currentPosition.progression,
            totalProgression = currentPosition.totalProgression,
            chapterIndex = currentPosition.chapterIndex,
            position = currentPosition.position,
            createdAt = now().toString(),
        )
        viewModelScope.launch {
            addBookmarkUseCase(bookmark)
                .onSuccess {
                    analytics.logEvent(ReaderAnalyticsEvent.BookmarkAdded(bookUuid = bookUuid))
                    updateState {
                        it.copy(
                            isBookmarksVisible = false,
                            showBookmarkAdded = true,
                            lastAddedBookmarkId = bookmark.id,
                        )
                    }
                }
                .onFailure { error ->
                    error.log(analytics, "ReaderViewModel: Failed to save bookmark")
                    updateState { it.copy(showBookmarkSaveFailed = true) }
                }
        }
    }

    private fun dismissBookmarkAdded() {
        updateState {
            it.copy(
                showBookmarkAdded = false,
                lastAddedBookmarkId = null,
            )
        }
    }

    private fun dismissBookmarkAlreadyExists() {
        updateState { it.copy(showBookmarkAlreadyExists = false) }
    }

    private fun undoBookmark(id: String) {
        viewModelScope.launch {
            deleteBookmarkUseCase(id)
        }
        updateState {
            it.copy(
                showBookmarkAdded = false,
                lastAddedBookmarkId = null,
            )
        }
    }

    private fun deleteBookmark(id: String) {
        viewModelScope.launch {
            deleteBookmarkUseCase(id)
        }
    }

    private fun renameBookmark(id: String, newTitle: String) {
        viewModelScope.launch {
            updateBookmarkTitleUseCase(id, newTitle)
        }
        updateState { it.copy(renamingBookmark = null) }
    }

    private fun reorderBookmarks(bookmarkIds: List<String>) {
        val orders = bookmarkIds.mapIndexed { index, id -> id to index }
        viewModelScope.launch {
            reorderBookmarksUseCase(orders)
        }
    }

    private fun goToPreviousBookmark() {
        val currentHref = viewState.value.currentPosition?.href ?: return
        val sorted = viewState.value.bookmarks.sortedBy { it.sortOrder }
        val currentIndex = sorted.indexOfFirst { it.locatorHref == currentHref }
        if (currentIndex <= 0) {
            updateState { it.copy(showNoMoreBookmarks = true) }
            return
        }
        goToBookmark(sorted[currentIndex - 1])
    }

    private fun goToNextBookmark() {
        val currentHref = viewState.value.currentPosition?.href ?: return
        val sorted = viewState.value.bookmarks.sortedBy { it.sortOrder }
        val currentIndex = sorted.indexOfFirst { it.locatorHref == currentHref }
        if (currentIndex < 0 || currentIndex >= sorted.size - 1) {
            updateState { it.copy(showNoMoreBookmarks = true) }
            return
        }
        goToBookmark(sorted[currentIndex + 1])
    }

    private fun dismissNoMoreBookmarks() {
        updateState { it.copy(showNoMoreBookmarks = false) }
    }

    private fun goToBookmark(bookmark: BookmarkUiModel) {
        val position = PositionUiModel(
            createdAt = bookmark.createdAt,
            href = bookmark.locatorHref,
            type = bookmark.locatorType ?: "",
            title = bookmark.locatorTitle,
            progression = bookmark.progression,
            position = bookmark.position,
            totalProgression = bookmark.totalProgression,
            chapterIndex = bookmark.chapterIndex,
            totalChapters = viewState.value.currentPosition?.totalChapters,
        )
        bookController.goToPosition(position)
        updateState { it.copy(isBookmarksVisible = false) }
    }

    private fun observeBookmarks() {
        observeBookmarksUseCase(bookUuid)
            .onEach { bookmarks ->
                updateState { state ->
                    state.copy(bookmarks = bookmarks.map { bookmark -> bookmark.toUiModel() })
                }
            }
            .launchIn(viewModelScope)
    }

    fun close() {
        viewModelScope.launch {
            // Only save audio position if this is a ReadAloud book with actual media overlays
            if (viewState.value.isReadAloud) {
                saveCurrentAudioPositionSync()
            }
            cancelSleepTimer()

            // Track book closed event with reading duration and progress
            val currentState = viewState.value
            val endTime = nowMillis()
            val readingDurationMs = if (bookOpenedTimestamp > 0) {
                endTime - bookOpenedTimestamp
            } else {
                0L
            }
            val progressPercent = currentState.currentPosition?.totalProgression
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
                    endProgression = currentState.currentPosition?.totalProgression,
                    readingSpeedWpm = sessionReadingSpeedWpm,
                )
            }

            // Update currently reading book if session was long enough (≥ 1 minute)
            if (readingDurationMs >= MINIMUM_READING_DURATION_MS && currentState.bookTitle.isNotEmpty()) {
                setCurrentlyReadingUseCase(
                    CurrentlyReadingDomainModel(
                        serverId = serverId,
                        bookUuid = bookUuid,
                        bookType = currentState.bookType,
                        bookTitle = currentState.bookTitle,
                        coverUrl = currentState.bookCoverUrl,
                        totalProgression = currentState.currentPosition?.totalProgression,
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

    private fun toggleAudioOnlyMode() {
        val isTurningOff = viewState.value.isAudioOnlyMode
        updateState { it.copy(isAudioOnlyMode = !it.isAudioOnlyMode) }
        if (isTurningOff) {
            audioController.currentAudioLocator.value?.locator?.let { locator ->
                bookController.goToLocator(locator)
            }
        }
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
        // Also save the speed to settings
        val currentSettings = viewState.value.currentSettings
        viewModelScope.launch {
            currentSettings?.let { settings ->
                saveReaderSettingsUseCase(settings.copy(playbackSpeed = speed).toDomainModel())
            }
        }
    }

    private fun startSleepTimer(durationMs: Long) {
        sleepTimerJob?.cancel()
        updateState {
            it.copy(
                sleepTimerRemainingMs = durationMs,
                showSleepTimerWarningPrompt = false,
            )
        }
        sleepTimerJob = viewModelScope.launch {
            var remainingMs = durationMs
            var hasShownWarning = false
            while (remainingMs > 0L) {
                delay(SLEEP_TIMER_TICK_MS)
                if (viewState.value.isPlaying) {
                    remainingMs = (remainingMs - SLEEP_TIMER_TICK_MS).coerceAtLeast(0L)
                    updateState {
                        it.copy(
                            sleepTimerRemainingMs = remainingMs,
                            showSleepTimerWarningPrompt = if (
                                !hasShownWarning &&
                                remainingMs in 1L..SLEEP_TIMER_WARNING_THRESHOLD_MS
                            ) {
                                true
                            } else {
                                it.showSleepTimerWarningPrompt
                            },
                        )
                    }
                    if (remainingMs in 1L..SLEEP_TIMER_WARNING_THRESHOLD_MS) {
                        hasShownWarning = true
                    }
                }
            }
            if (viewState.value.isPlaying) {
                audioController.togglePlayback()
                saveCurrentAudioPosition()
            }
            updateState {
                it.copy(
                    sleepTimerRemainingMs = null,
                    showSleepTimerWarningPrompt = false,
                )
            }
            sleepTimerJob = null
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        updateState {
            it.copy(
                sleepTimerRemainingMs = null,
                showSleepTimerWarningPrompt = false,
            )
        }
    }

    private fun dismissSleepTimerWarning() {
        updateState { it.copy(showSleepTimerWarningPrompt = false) }
    }

    private fun setHighlightColor(colorArgb: Int) {
        val currentSettings = viewState.value.currentSettings ?: return
        val updatedSettings = currentSettings.copy(highlightColor = colorArgb)
        updatePublicationState { it.copy(settings = updatedSettings) }
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
        val currentPosition = currentState.currentPosition

        if (audioPositionMs <= 0) {
            return
        }
        if (currentPosition == null) {
            return
        }


        val now = now().toString()
        val positionDomainModel = PositionDomainModel(
            bookUuid = bookUuid,
            serverId = serverId,
            timestamp = nowMillis(),
            createdAt = currentPosition.createdAt,
            updatedAt = now,
            locatorHref = currentPosition.href,
            locatorType = currentPosition.type,
            locatorTitle = currentPosition.title,
            locatorTarget = null,
            audioTimestampMs = audioPositionMs,
            chapterIndex = currentPosition.chapterIndex,
            progression = currentPosition.progression,
            totalChapters = currentPosition.totalChapters,
            totalDurationMs = currentState.totalDurationMs,
            totalProgression = currentPosition.totalProgression,
            position = currentPosition.position,
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

        private const val SLEEP_TIMER_TICK_MS = 1_000L

        private const val SLEEP_TIMER_WARNING_THRESHOLD_MS = 60_000L
    }
}
