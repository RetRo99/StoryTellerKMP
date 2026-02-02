package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.model.ReaderSettingsUiModel

/**
 * Controller interface for EPUB navigation and settings.
 *
 * This controller is owned by the View layer and handles:
 * - Page navigation (next/previous)
 * - Chapter navigation
 * - Reader settings application
 *
 * Platform implementations wrap the native navigator components:
 * - Android: [EpubNavigatorFragment] from Readium
 * - iOS: EPUBNavigatorViewController via bridge
 *
 * The View creates this controller after the publication is ready and
 * uses it to execute navigation commands from the ViewModel.
 */
interface EpubNavigatorController {

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
}

