package com.retro99.reader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.retro99.base.nowMillis
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.base.ui.LoadingScreen
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.TocItemUiModel
import com.retro99.reader.ui.publication.EpubPublication
import com.retro99.translations.StringRes
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.reader_toc_jumped_to_chapter
import resources.translations.reader_toc_undo
import resources.translations.settings_icon_content_description

/** Duration in milliseconds before auto-hiding the media controls */
private const val CONTROLS_AUTO_HIDE_DELAY_MS = 5000L

@Composable
fun ReaderScreen(
    bookUuid: String,
    bookType: BookType,
    onClose: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = koinViewModel {
        parametersOf(bookUuid, bookType, onClose, onSettingsClick)
    },
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        ReaderScreenContent(
            bookUuid = bookUuid,
            viewState = viewState,
            intentDispatcher = intentDispatcher,
        )
    }
}

@Composable
private fun ReaderScreenContent(
    bookUuid: String,
    viewState: ReaderViewState,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {

        val movableLoader = movableContentOf {
            LoadingScreen()
        }
        if (viewState.publication != null) {
            ReaderContent(
                bookUuid = bookUuid,
                publication = viewState.publication,
                currentSettings = viewState.currentSettings,
                isReadAloud = viewState.isReadAloud,
                isPlaying = viewState.isPlaying,
                currentAudioPositionMs = viewState.currentAudioPositionMs,
                totalDurationMs = viewState.totalDurationMs,
                playbackSpeed = viewState.playbackSpeed,
                isAudioPlayerReady = viewState.isAudioPlayerReady,
                tableOfContents = viewState.tableOfContents,
                isTocVisible = viewState.isTocVisible,
                previousTocPosition = viewState.previousTocPosition,
                lastKnownPosition = viewState.lastKnownPosition,
                intentDispatcher = intentDispatcher,
                loader = movableLoader,
            )
        } else {
            movableLoader()
        }

        viewState.positionConflict?.let { conflict ->
            PositionConflictDialog(
                conflict = conflict,
                onUseLocal = { intentDispatcher(ReaderIntent.UseLocalPosition) },
                onUseRemote = { intentDispatcher(ReaderIntent.UseRemotePosition) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderContent(
    bookUuid: String,
    publication: EpubPublication,
    currentSettings: ReaderSettingsUiModel?,
    isReadAloud: Boolean,
    isPlaying: Boolean,
    currentAudioPositionMs: Long,
    totalDurationMs: Long?,
    playbackSpeed: Float,
    tableOfContents: List<TocItemUiModel>,
    isTocVisible: Boolean,
    previousTocPosition: PositionUiModel?,
    lastKnownPosition: PositionUiModel?,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    isAudioPlayerReady: Boolean,
    loader: @Composable (() -> Unit),
) {
    val settings = currentSettings ?: publication.initialSettings
    var tempScale by remember(settings.fontSize) { mutableStateOf(settings.fontSize) }
    var isZooming by remember { mutableStateOf(false) }

    var areControlsVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(0L) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(areControlsVisible, lastInteractionTime) {
        if (areControlsVisible) {
            delay(CONTROLS_AUTO_HIDE_DELAY_MS)
            areControlsVisible = false
        }
    }

    val onControlsInteraction: () -> Unit = {
        lastInteractionTime = nowMillis()
        areControlsVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it },
    ) {
        EpubReaderView(
            bookUuid = bookUuid,
            publication = publication,
            intentDispatcher = intentDispatcher,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = settings.marginVertical.dp)
                .readerGestures(
                    containerSize = containerSize,
                    onZoomChange = { scale ->
                        isZooming = true
                        tempScale = (settings.fontSize * scale).coerceIn(0.5, 3.0)
                        onControlsInteraction()
                    },
                    onZoomEnd = { finalScale ->
                        val newFontSize = (settings.fontSize * finalScale).coerceIn(0.5, 3.0)
                        intentDispatcher(
                            ReaderIntent.UpdateSettings(
                                settings.copy(fontSize = newFontSize)
                            )
                        )
                        isZooming = false
                    },
                    onLeftTap = {
                        intentDispatcher(ReaderIntent.GoToPreviousPage)
                    },
                    onRightTap = {
                        intentDispatcher(ReaderIntent.GoToNextPage)
                    },
                    onMiddleTap = {
                        areControlsVisible = !areControlsVisible
                        if (areControlsVisible) {
                            lastInteractionTime = nowMillis()
                        }
                    },
                ),
        )

        // Loading overlay for ReadAloud books while audio player initializes
        if (isReadAloud && !isAudioPlayerReady) {
            loader()
        }

        // Visual Overlay (Shows only while pinching)
        if (isZooming) {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "${(tempScale * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        if (isReadAloud && isAudioPlayerReady) {
            AnimatedVisibility(
                visible = areControlsVisible,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                ReadAloudControls(
                    isPlaying = isPlaying,
                    currentPositionMs = currentAudioPositionMs,
                    totalDurationMs = totalDurationMs,
                    playbackSpeed = playbackSpeed,
                    intentDispatcher = intentDispatcher,
                    onInteraction = onControlsInteraction,
                    onSwipeDown = { areControlsVisible = false },
                )
            }
        }

        // Top toolbar with settings and TOC icons
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            ReaderToolbar(
                onTocClick = { intentDispatcher(ReaderIntent.ToggleToc) },
                onSettingsClick = { intentDispatcher(ReaderIntent.OnSettingsClicked) },
                onInteraction = onControlsInteraction,
            )
        }

        // Table of Contents bottom sheet
        if (isTocVisible) {
            TableOfContentsSheet(
                tableOfContents = tableOfContents,
                onChapterClick = { href ->
                    intentDispatcher(ReaderIntent.GoToChapter(href, lastKnownPosition))
                },
                onDismiss = { intentDispatcher(ReaderIntent.ToggleToc) },
            )
        }

        ChapterNavigationUndoSnackbar(
            previousTocPosition = previousTocPosition,
            onUndo = { position ->
                intentDispatcher(ReaderIntent.UndoChapterNavigation(position))
            },
            onDismiss = {
                intentDispatcher(ReaderIntent.DismissChapterNavigationUndo)
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ReaderToolbar(
    onTocClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row {
                IconButton(
                    onClick = {
                        onInteraction()
                        onTocClick()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Table of Contents",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(
                    onClick = {
                        onInteraction()
                        onSettingsClick()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(StringRes.settings_icon_content_description),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterNavigationUndoSnackbar(
    previousTocPosition: PositionUiModel?,
    onUndo: (PositionUiModel) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val jumpedToChapterMessage = stringResource(StringRes.reader_toc_jumped_to_chapter)
    val undoLabel = stringResource(StringRes.reader_toc_undo)

    LaunchedEffect(previousTocPosition) {
        if (previousTocPosition != null) {
            val result = snackbarHostState.showSnackbar(
                message = jumpedToChapterMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> onUndo(previousTocPosition)
                SnackbarResult.Dismissed -> onDismiss()
            }
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
    ) { snackbarData ->
        val dismissState = rememberSwipeToDismissBoxState()
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                snackbarData.dismiss()
            }
        }
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
        ) {
            Snackbar(snackbarData = snackbarData)
        }
    }
}
