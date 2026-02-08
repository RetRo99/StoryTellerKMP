package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.bridge.AudioLocator
import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.bridge.EpubReaderSettings
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.annotation.Single

/**
 * iOS implementation of [BookController].
 * Delegates navigation and settings operations to the [EpubReaderBridge].
 */
@Single
class IosBookController(
    private val bridge: EpubReaderBridge,
) : BookController {

    private val _currentLocator = MutableSharedFlow<LocatorState>(
        replay = 1,
        extraBufferCapacity = 1,
    )
    override val currentLocator: Flow<LocatorState> = _currentLocator

    init {
        setupCallbacks()
    }

    private fun setupCallbacks() {
        bridge.setOnPositionChangedCallback { locator ->
            _currentLocator.tryEmit(
                LocatorState(
                    href = locator.href,
                    type = locator.type,
                    title = locator.title,
                    progression = locator.progression,
                    position = locator.position,
                    totalProgression = locator.totalProgression,
                    fragments = null,
                ),
            )
        }
    }

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

    override fun goToPosition(position: PositionUiModel) {
        bridge.goToPosition(
            href = position.href,
            type = position.type,
            progression = position.progression,
            position = position.position,
        )
    }

    override suspend fun applyHighlight(locator: LocatorState) {
        bridge.applyAudioHighlight(locator.toAudioLocator())
    }

    override fun close() {
        bridge.setOnPositionChangedCallback(null)
    }
}

private fun LocatorState.toAudioLocator(): AudioLocator {
    return AudioLocator(
        href = href,
        type = type,
        title = title,
        progression = progression,
        position = position,
        totalProgression = totalProgression,
        fragment = fragments?.firstOrNull(),
    )
}
