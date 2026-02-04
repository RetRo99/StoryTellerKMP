package com.retro99.reader.ui.reader

import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel

/**
 * Commands emitted by the ViewModel for the View to execute.
 *
 * This sealed class represents navigation and settings commands that the ViewModel
 * needs to communicate to the View. The View collects these commands via a SharedFlow
 * and executes them using its [EpubNavigatorController].
 *
 * This pattern maintains unidirectional data flow:
 * - ViewModel tells *what* to do (emits commands)
 * - View decides *how* to do it (executes using platform navigator)
 */
sealed class ReaderCommand {

    /**
     * Command to navigate to the next page.
     */
    data object GoToNextPage : ReaderCommand()

    /**
     * Command to navigate to the previous page.
     */
    data object GoToPreviousPage : ReaderCommand()

    /**
     * Command to navigate to a specific chapter.
     *
     * @param href The href of the chapter to navigate to
     */
    data class GoToChapter(val href: String) : ReaderCommand()

    /**
     * Command to apply reader settings.
     *
     * @param settings The reader settings to apply
     */
    data class ApplySettings(val settings: ReaderSettingsUiModel) : ReaderCommand()

    /**
     * Command to navigate to a specific position.
     * Used when resolving position conflicts.
     *
     * @param position The position to navigate to
     */
    data class GoToPosition(val position: PositionUiModel) : ReaderCommand()
}

