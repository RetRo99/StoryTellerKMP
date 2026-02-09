package com.retro99.reader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.publication.EpubPublication
import com.retro99.translations.StringRes
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
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
            bookController = viewModel.bookController,
        )
    }
}

@Composable
private fun ReaderScreenContent(
    bookUuid: String,
    viewState: ReaderViewState,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    bookController: BookController,
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
                loader = movableLoader,
                bookController = bookController,
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
    isAudioPlayerReady: Boolean,
    loader: @Composable (() -> Unit),
    bookController: BookController,
) {
    val settings = publication.initialSettings
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
            bookController = bookController,
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

        // Top toolbar with settings icon
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            ReaderToolbar(
                onSettingsClick = { intentDispatcher(ReaderIntent.OnSettingsClicked) },
                onInteraction = onControlsInteraction,
            )
        }
    }
}

@Composable
private fun ReaderToolbar(
    onSettingsClick: () -> Unit,
    onInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
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

