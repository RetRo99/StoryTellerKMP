package com.retro99.reader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Duration in milliseconds before auto-hiding the media controls */
private const val CONTROLS_AUTO_HIDE_DELAY_MS = 5000L

@Composable
fun ReaderScreen(
    bookUuid: String,
    bookType: BookType,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = koinViewModel {
        parametersOf(bookUuid, bookType, onClose)
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
            commands = viewModel.commands,
        )
    }
}

@Composable
private fun ReaderScreenContent(
    bookUuid: String,
    viewState: ReaderViewState,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    commands: Flow<ReaderCommand>,
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
                isReadAloud = viewState.isReadAloud,
                isPlaying = viewState.isPlaying,
                currentAudioPositionMs = viewState.currentAudioPositionMs,
                totalDurationMs = viewState.totalDurationMs,
                playbackSpeed = viewState.playbackSpeed,
                isAudioPlayerReady = viewState.isAudioPlayerReady,
                intentDispatcher = intentDispatcher,
                commands = commands,
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

@Composable
private fun ReaderContent(
    bookUuid: String,
    publication: EpubPublication,
    isReadAloud: Boolean,
    isPlaying: Boolean,
    currentAudioPositionMs: Long,
    totalDurationMs: Long?,
    playbackSpeed: Float,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    commands: Flow<ReaderCommand>,
    isAudioPlayerReady: Boolean,
    loader: @Composable (() -> Unit),
) {
    val settings = publication.initialSettings
    var tempScale by remember(settings.fontSize) { mutableStateOf(settings.fontSize) }
    var isZooming by remember { mutableStateOf(false) }

    var areControlsVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(0L) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(areControlsVisible, lastInteractionTime) {
        if (areControlsVisible && isReadAloud) {
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
            commands = commands,
            intentDispatcher = intentDispatcher,
            modifier = Modifier
                .fillMaxSize()
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
                        onControlsInteraction()
                    },
                    onRightTap = {
                        intentDispatcher(ReaderIntent.GoToNextPage)
                        onControlsInteraction()
                    },
                    onMiddleTap = {
                        if (isReadAloud) {
                            areControlsVisible = !areControlsVisible
                            if (areControlsVisible) {
                                lastInteractionTime = nowMillis()
                            }
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
    }
}

