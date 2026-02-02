package com.retro99.reader.ui.publication

import com.retro99.reader.domain.model.InitialLocatorDomainModel
import com.retro99.reader.ui.bridge.EpubLocator
import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.bridge.EpubReaderSettings
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.navigator.EpubNavigatorController

/**
 * iOS implementation of EpubPublication.
 * Wraps the EpubReaderBridge which provides access to the Swift Readium implementation.
 *
 * This class implements [EpubNavigatorController] directly, eliminating the need for
 * a separate navigator wrapper. Since it already holds the bridge, it can delegate
 * all navigation operations directly.
 */
actual class EpubPublication(
    internal val bridge: EpubReaderBridge,
    actual val initialSettings: ReaderSettingsUiModel,
    private val initialLocator: InitialLocatorDomainModel?,
) : EpubNavigatorController {

    /**
     * Gets the initial locator as an EpubLocator for the bridge.
     */
    internal val initialEpubLocator: EpubLocator?
        get() = initialLocator?.let {
            EpubLocator(
                href = it.href,
                type = it.type,
                title = it.title,
                progression = it.progression,
                position = it.position,
                totalProgression = it.totalProgression,
            )
        }

    /**
     * Closes the publication and releases resources.
     */
    actual fun close() {
        bridge.closePublication()
    }

    // EpubNavigatorController implementation - delegates to bridge

    override fun goToNextPage() {
        bridge.goToNextPage()
    }

    override fun goToPreviousPage() {
        bridge.goToPreviousPage()
    }

    override fun goToChapter(href: String) {
        bridge.goToChapter(href)
    }

    override fun setSettings(settings: ReaderSettingsUiModel) {
        bridge.setSettings(settings = EpubReaderSettings.from(settings))
    }
}

