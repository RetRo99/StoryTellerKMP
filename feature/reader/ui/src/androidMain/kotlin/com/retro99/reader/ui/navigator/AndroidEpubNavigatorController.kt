package com.retro99.reader.ui.navigator

import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Link

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

    override fun goToNextPage() {
        navigator.goForward()
    }

    override fun goToPreviousPage() {
        navigator.goBackward()
    }

    override fun goToChapter(href: String) {
        val link = Link(href = href)
        navigator.go(link)
    }

    override fun setSettings(settings: ReaderSettingsDomainModel) {
        navigator.submitPreferences(settings.toEpubPreferences())
    }

    /**
     * Converts reader settings to Readium EpubPreferences.
     * This is used both for initial preferences and dynamic updates.
     * Add new preference mappings here as needed.
     */
    private fun ReaderSettingsDomainModel.toEpubPreferences(): EpubPreferences {
        return EpubPreferences(
            fontSize = fontSize,
            // Add more preferences here as needed:
            // fontFamily = fontFamily,
            // lineHeight = lineHeight,
            // etc.
        )
    }
}

/**
 * Extension function to convert ReaderSettingsDomainModel to EpubPreferences.
 * This is used for initial preferences when creating the navigator.
 */
fun ReaderSettingsDomainModel.toEpubPreferences(): EpubPreferences {
    return EpubPreferences(
        fontSize = fontSize,
        // Add more preferences here as needed:
        // fontFamily = fontFamily,
        // lineHeight = lineHeight,
        // etc.
    )
}

