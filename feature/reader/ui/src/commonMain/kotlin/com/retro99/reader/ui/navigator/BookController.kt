package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import kotlinx.coroutines.flow.Flow

interface BookController : AutoCloseable {

    /**
     * Flow of current reading position/locator changes.
     * Emits whenever the user navigates to a new position.
     */
    val currentLocator: Flow<LocatorState>

    /**
     * Navigates to the next page.
     */
    fun goToNextPage()

    /**
     * Navigates to the previous page.
     */
    fun goToPreviousPage()

    /**
     * Navigates to a specific chapter by its href.
     *
     * @param href The href of the chapter to navigate to
     */
    fun goToChapter(href: String)

    /**
     * Applies the given reader settings.
     *
     * @param settings The reader settings to apply
     */
    fun setSettings(settings: ReaderSettingsUiModel)

    /**
     * Navigates to a specific position in the publication.
     *
     * @param position The position to navigate to
     */
    fun goToPosition(position: PositionUiModel)

}