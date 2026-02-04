package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import kotlinx.coroutines.flow.StateFlow
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

/**
 * Android implementation of [EpubNavigatorController] using Readium's EpubNavigatorFragment.
 *
 * This controller wraps the Readium navigator fragment and provides navigation
 * and settings functionality. It is created by the View after the publication
 * is ready and the navigator fragment is instantiated.
 *
 * @param navigator The Readium EpubNavigatorFragment to wrap
 */
class AndroidEpubNavigatorController(
    private val navigator: EpubNavigatorFragment,
) : EpubNavigatorController {

    /**
     * StateFlow of the current locator, emitting location changes as the user navigates.
     */
    val currentLocator: StateFlow<Locator> = navigator.currentLocator

    override fun goToNextPage() {
        navigator.goForward()
    }

    override fun goToPreviousPage() {
        navigator.goBackward()
    }

    override fun goToChapter(href: String) {
        val url = Url(href) ?: return
        val link = Link(href = url)
        navigator.go(link)
    }

    override fun setSettings(settings: ReaderSettingsUiModel) {
        navigator.submitPreferences(settings.toEpubPreferences())
    }

    override fun goToPosition(position: PositionUiModel) {
        val url = Url(position.href) ?: return
        val locator = Locator(
            href = url,
            mediaType = MediaType(position.type) ?: return,
            title = position.title,
            locations = Locator.Locations(
                progression = position.progression,
                position = position.position,
                totalProgression = position.totalProgression,
            ),
        )
        navigator.go(locator)
    }

    /**
     * Converts reader settings to Readium EpubPreferences.
     * This is used both for initial preferences and dynamic updates.
     * Add new preference mappings here as needed.
     */
    private fun ReaderSettingsUiModel.toEpubPreferences(): EpubPreferences {
        return EpubPreferences(
            fontSize = fontSize,
            scroll = scrollMode,
            // Add more preferences here as needed:
            // fontFamily = fontFamily,
            // lineHeight = lineHeight,
            // etc.
        )
    }
}

/**
 * Extension function to convert ReaderSettingsUiModel to EpubPreferences.
 * This is used for initial preferences when creating the navigator.
 */
fun ReaderSettingsUiModel.toEpubPreferences(): EpubPreferences {
    return EpubPreferences(
        fontSize = fontSize,
        scroll = scrollMode,
        // Add more preferences here as needed:
        // fontFamily = fontFamily,
        // lineHeight = lineHeight,
        // etc.
    )
}

