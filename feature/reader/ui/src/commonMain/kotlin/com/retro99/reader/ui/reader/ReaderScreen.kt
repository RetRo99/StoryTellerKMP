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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
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
        if (viewState.isPublicationReady) {
            val logger = Logger.withTag("čič")
            SideEffect {
                logger.d { "showing ui settings: ${viewState.settings}" }
            }
            ReaderContent(
                bookUuid = bookUuid,
                settings = viewState.settings,
                intentDispatcher = intentDispatcher,
            )
        }
    }
}

@Composable
private fun ReaderContent(
    bookUuid: String,
    settings: ReaderSettingsDomainModel,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
) {
    // 1. Sync local state with the real source of truth
    var tempScale by remember(settings.fontSize) { mutableStateOf(settings.fontSize) }

    // 2. Used to display an overlay while zooming (optional but good UX)
    var isZooming by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Custom detector to handle "End of Gesture"
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.size >= 2) {
                            val zoomChange = event.calculateZoom()
                            if (zoomChange != 1f) {
                                isZooming = true
                                tempScale = (tempScale * zoomChange).coerceIn(0.5, 3.0)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    if (isZooming) {
                        intentDispatcher(
                            ReaderIntent.UpdateSettings(
                                settings.copy(
                                    fontSize = tempScale,
                                )
                            )
                        )
                        isZooming = false
                    }
                }
            }
    ) {
        EpubReaderView(
            bookUuid = bookUuid,
            initialSettings = settings,
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

