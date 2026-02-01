package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.retro99.reader.ui.navigator.EpubNavigatorController
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific EPUB reader view.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 *
 * @param bookUuid The unique identifier of the book
 * @param publication The opened EPUB publication
 * @param settings The reader settings to apply (reactive - updates when changed)
 * @param commands Flow of commands from ViewModel for navigation and settings
 * @param onProgressChanged Callback when the reading progress changes
 * @param modifier The modifier to apply to the view
 */
@Composable
internal expect fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    commands: Flow<ReaderCommand>,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier = Modifier,
)

/**
 * Common composable that handles command execution for a navigator controller.
 * This should be called by platform implementations when the navigator is ready.
 *
 * @param navigator The navigator controller to execute commands on
 * @param commands Flow of commands from ViewModel
 */
@Composable
internal fun HandleNavigatorCommands(
    navigator: EpubNavigatorController?,
    commands: Flow<ReaderCommand>,
) {
    // Collect and execute navigation commands
    LaunchedEffect(navigator) {
        navigator?.let { controller ->
            commands.collect { command ->
                when (command) {
                    is ReaderCommand.GoToNextPage -> controller.goToNextPage()
                    is ReaderCommand.GoToPreviousPage -> controller.goToPreviousPage()
                    is ReaderCommand.GoToChapter -> controller.goToChapter(command.href)
                    is ReaderCommand.ApplySettings -> controller.setSettings(command.settings)
                }
            }
        }
    }
}

