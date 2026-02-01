package com.retro99.reader.ui.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.publication.EpubPublication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReaderScreen(
    bookUuid: String,
    ebookFilePath: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = koinViewModel { parametersOf(bookUuid, ebookFilePath, onClose) },
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
    commands: kotlinx.coroutines.flow.Flow<ReaderCommand>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (viewState.publication != null) {
            ReaderContent(
                bookUuid = bookUuid,
                publication = viewState.publication,
                intentDispatcher = intentDispatcher,
                commands = commands,
            )
        }
    }
}

@Composable
private fun ReaderContent(
    bookUuid: String,
    publication: EpubPublication,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    commands: kotlinx.coroutines.flow.Flow<ReaderCommand>,
) {
    // Get initial settings from the publication
    val settings = publication.initialSettings
    var tempScale by remember(settings.fontSize) { mutableStateOf(settings.fontSize) }
    var isZooming by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(settings.fontSize) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var zoomAccumulator = 1f
                    var gestureActive = false

                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.size >= 2) {
                            val zoomChange = event.calculateZoom()

                            if (!gestureActive) {
                                zoomAccumulator *= zoomChange
                                val isZoomingOut = zoomAccumulator < 0.80f
                                val isZoomingIn = zoomAccumulator > 1.20f

                                if (isZoomingIn || isZoomingOut) {
                                    gestureActive = true
                                    isZooming = true
                                    tempScale = (tempScale * zoomAccumulator).coerceIn(0.5, 3.0)
                                }
                            } else {
                                if (zoomChange != 1f) {
                                    tempScale = (tempScale * zoomChange).coerceIn(0.5, 3.0)
                                }
                                event.changes.forEach {
                                    if (it.positionChanged()) it.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // 4. Gesture Ended: Save to DB
                    if (isZooming) {
                        intentDispatcher(
                            ReaderIntent.UpdateSettings(
                                settings.copy(fontSize = tempScale)
                            )
                        )
                        isZooming = false
                    }
                }
            }
    ) {
        EpubReaderView(
            bookUuid = bookUuid,
            publication = publication,
            settings = settings,
            commands = commands,
            onProgressChanged = { locator, progression ->
                intentDispatcher(ReaderIntent.UpdateProgress(locator, progression))
            },
            modifier = Modifier.fillMaxSize(),
        )

        // 4. Optional: Visual Overlay (Shows only while pinching)
        if (isZooming) {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "${(tempScale * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

