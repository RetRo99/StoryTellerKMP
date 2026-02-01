package com.retro99.reader.ui.navigator

import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.bridge.EpubReaderSettings

/**
 * iOS implementation of [EpubNavigatorController].
 *
 * This controller uses the [EpubReaderBridge] to delegate navigation and settings
 * to the Swift Readium implementation. It is created by the View after the
 * publication is ready and the reader view controller is instantiated.
 *
 * @param bridge The EpubReaderBridge to use for navigation
 */
class IosEpubNavigatorController(
    private val bridge: EpubReaderBridge,
) : EpubNavigatorController {

    override fun goToNextPage() {
        bridge.goToNextPage()
    }

    override fun goToPreviousPage() {
        bridge.goToPreviousPage()
    }

    override fun goToChapter(href: String) {
        bridge.goToChapter(href)
    }

    override fun setSettings(settings: ReaderSettingsDomainModel) {
        bridge.setSettings(settings = EpubReaderSettings.from(settings))
    }
}

