package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

/** Decoration group name for ReadAloud text highlighting */
private const val READALOUD_DECORATION_GROUP = "readaloud"

/** Highlight color for the currently spoken text (semi-transparent yellow) */
private const val READALOUD_HIGHLIGHT_COLOR = 0x80FFEB3B.toInt()

/**
 * Android implementation of [EpubNavigatorController] using Readium's EpubNavigatorFragment.
 *
 * This controller wraps the Readium navigator fragment and provides navigation
 * and settings functionality. It is created by the View after the publication
 * is ready and the navigator fragment is instantiated.
 *
 * For ReadAloud books with media overlays, this controller delegates audio playback
 * to [AndroidAudioController] and handles text highlighting in the navigator.
 *
 * @param context Android context for creating the media player
 * @param analytics Analytics instance for logging errors and events
 * @param smilParser Parser for SMIL media overlay files
 * @param quickScanner Quick scanner for SMIL file indexing
 */
@Single(
    binds = [
        BookController::class,
    ]
)
class AndroidEpubNavigatorController internal constructor() : EpubNavigatorController {

    private val _navigator = MutableStateFlow<EpubNavigatorFragment?>(null)

    private val navigator: EpubNavigatorFragment
        get() = _navigator.value ?: error("Navigator not initialized")

    fun init(
        navigator: EpubNavigatorFragment,
    ) {
        _navigator.value = navigator
    }

    /**
     * Flow of current reading position/locator changes.
     * Converts Readium's Locator to the common LocatorState model.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentLocator: Flow<LocatorState> = _navigator.flatMapLatest { navigator ->
        navigator?.currentLocator?.map { locator ->
            LocatorState(
                href = locator.href.toString(),
                type = locator.mediaType.toString(),
                title = locator.title,
                progression = locator.locations.progression,
                position = locator.locations.position,
                totalProgression = locator.locations.totalProgression,
                fragments = locator.locations.fragments,
            )
        } ?: flowOf() // or emptyFlow()
    }

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
        _navigator.value?.submitPreferences(settings.toEpubPreferences())
    }

    override fun goToPosition(position: PositionUiModel) {
        val locator = position.toAndroidLocator() ?: return
        navigator.go(locator)
    }

    /**
     * Applies a highlight decoration to the given locator and navigates to it.
     * This ensures the currently spoken text is always visible on screen.
     */
    override suspend fun applyHighlight(locator: LocatorState) {
        val decorableNavigator = navigator as? DecorableNavigator ?: return
        val locator = locator.toAndroidLocator() ?: return
        val decoration = Decoration(
            id = "readaloud-active",
            locator = locator,
            style = Decoration.Style.Highlight(
                tint = READALOUD_HIGHLIGHT_COLOR,
                isActive = true,
            ),
        )

        decorableNavigator.applyDecorations(listOf(decoration), READALOUD_DECORATION_GROUP)

        // Navigate to the locator to ensure the highlighted text is visible on screen
        // This is especially important when seeking audio - the text should follow
        navigator.go(locator)
    }

    override fun close() {

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

/**
 * Extension function to convert PositionUiModel to Readium Locator.
 * Returns null if the URL or mediaType cannot be parsed.
 */
fun PositionUiModel.toAndroidLocator(): Locator? {
    val url = Url(href) ?: return null
    val mediaType = MediaType(type) ?: return null

    return Locator(
        href = url,
        mediaType = mediaType,
        title = title,
        locations = Locator.Locations(
            progression = progression,
            position = position,
            totalProgression = totalProgression,
        ),
    )
}

fun LocatorState.toAndroidLocator(): Locator? {
    val url = Url(href) ?: return null
    val mediaType = MediaType(type) ?: return null

    return Locator(
        href = url,
        mediaType = mediaType,
        title = title,
        locations = Locator.Locations(
            progression = progression,
            position = position,
            totalProgression = totalProgression,
            fragments = fragments ?: emptyList(),
        ),
    )
}