package com.retro99.reader.ui.navigator

import co.touchlab.kermit.Logger
import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.model.ChapterPageInfo
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReadAloudHighlightColor
import com.retro99.reader.ui.model.ReadAloudHighlightStyle
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.ReaderTextAlignUi
import com.retro99.reader.ui.model.ReaderThemeUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.TextAlign
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

/** Decoration group name for ReadAloud text highlighting */
private const val READALOUD_DECORATION_GROUP = "readaloud"

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
@Scope(ReaderScope::class)
@Scoped(binds = [BookController::class])
class AndroidEpubNavigatorController internal constructor() : EpubNavigatorController {

    private val _navigator = MutableStateFlow<EpubNavigatorFragment?>(null)
    private var controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingPageTurnJob: Job? = null

    /**
     * Current reader settings, used for highlight color.
     */
    private var highLightColor: ReadAloudHighlightColor = ReadAloudHighlightColor.YELLOW

    /**
     * Current highlight style setting.
     */
    private var highlightStyle: ReadAloudHighlightStyle = ReadAloudHighlightStyle.HIGHLIGHT

    /**
     * Tracks the last sentence that triggered a page turn to avoid duplicate turns.
     */
    private var lastPageTurnSentenceId: String? = null

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
        highLightColor = settings.highlightColor
        highlightStyle = settings.highlightStyle
        _navigator.value?.submitPreferences(settings.toEpubPreferences())
    }

    override fun goToPosition(position: PositionUiModel) {
        val locator = position.toAndroidLocator() ?: return
        navigator.go(locator)
    }

    /**
     * Applies a highlight decoration to the given locator and handles split sentences.
     *
     * For sentences that are split across pages, this method will:
     * 1. Apply the highlight immediately
     * 2. Check if the sentence is split (partially visible)
     * 3. Schedule a page turn after the visible portion has been read
     */
    override suspend fun applyHighlightWithPageTurn(
        locator: LocatorState,
        sentenceDurationMs: Long,
    ) {
        val decorableNavigator = _navigator.value as? DecorableNavigator ?: return
        val androidLocator = locator.toAndroidLocator() ?: return
        val fragmentId = locator.fragments?.firstOrNull()

        val decorations = createDecorations(androidLocator)
        decorableNavigator.applyDecorations(decorations, READALOUD_DECORATION_GROUP)

        // Check visibility and handle page turn if needed
        if (fragmentId != null) {
            val visibility = checkSentenceVisibility(fragmentId)
            logger.d { "Visibility: $visibility" }
            if (visibility.needsPageTurn && fragmentId != lastPageTurnSentenceId) {
                // Cancel any pending page turn from a previous sentence
                pendingPageTurnJob?.cancel()

                // Calculate delay based on visible fraction
                val delayMs = (visibility.visibleFraction * sentenceDurationMs).toLong()
                    .coerceAtLeast(MIN_PAGE_TURN_DELAY_MS)

                logger.d {
                    "Scheduling page turn for '$fragmentId' - " +
                            "visible: ${(visibility.visibleFraction * 100).toInt()}%, " +
                            "delay: ${delayMs}ms"
                }

                lastPageTurnSentenceId = fragmentId
                pendingPageTurnJob = controllerScope.launch {
                    delay(delayMs)
                    logger.d { "Executing pre-emptive page turn for '$fragmentId'" }
                    navigator.goForward()
                }
            } else if (!visibility.needsPageTurn) {
                // Sentence is fully visible, cancel any pending page turn
                pendingPageTurnJob?.cancel()
                lastPageTurnSentenceId = null
            }
        }
    }

    /**
     * Checks the visibility of a sentence element on the current page using JavaScript.
     *
     * Uses `getClientRects()` to get the bounding rectangles for each line of the sentence.
     * In paginated EPUB mode, lines that are on the next virtual page will have their
     * left edge beyond the viewport width.
     *
     * A page turn is triggered if:
     * 1. The sentence is entirely on the next page (all lines off-screen)
     * 2. Less than 50% of the sentence is visible
     * 3. The sentence starts in the "awkward buffer" (last 10% of page width)
     *
     * @param elementId The ID of the sentence element to check
     * @return The visibility result including visible fraction and whether page turn is needed
     */
    override suspend fun checkSentenceVisibility(elementId: String): SentenceVisibilityResult {
        val nav = _navigator.value
            ?: return SentenceVisibilityResult.FULLY_VISIBLE

        val script = SentenceVisibilityChecker.getVisibilityCheckScript(elementId)

        // Execute JS to get the element's geometry relative to the viewport
        val rawResult = nav.evaluateJavascript(script)
            ?: return SentenceVisibilityResult.FULLY_VISIBLE

        val cleanJson = cleanWebViewJson(rawResult)
        return SentenceVisibilityChecker.parseVisibilityResult(cleanJson, elementId)
    }

    override suspend fun getChapterPageInfo(): ChapterPageInfo? {
        val nav = _navigator.value ?: return null

        val script = ChapterPageCalculator.getPageCalculationScript()
        val rawResult = nav.evaluateJavascript(script) ?: return null

        val cleanJson = cleanWebViewJson(rawResult)
        return ChapterPageCalculator.parsePageResult(cleanJson)
    }

    /**
     * Cleans JSON returned from WebView's evaluateJavascript.
     *
     * WebView returns strings as "\"{\\\"key\\\":\\\"val\\\"}\"".
     * We must strip the outer quotes and unescape.
     */
    private fun cleanWebViewJson(rawResult: String): String {
        return if (rawResult.startsWith("\"") && rawResult.endsWith("\"")) {
            rawResult.substring(1, rawResult.length - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        } else {
            rawResult
        }
    }

    /**
     * Creates decorations based on the current highlight style setting.
     */
    private fun createDecorations(locator: Locator): List<Decoration> {
        return when (highlightStyle) {
            ReadAloudHighlightStyle.HIGHLIGHT -> listOf(
                Decoration(
                    id = "readaloud-highlight",
                    locator = locator,
                    style = Decoration.Style.Highlight(
                        tint = highLightColor.argb,
                        isActive = false,
                    ),
                ),
            )

            ReadAloudHighlightStyle.UNDERLINE -> listOf(
                Decoration(
                    id = "readaloud-underline",
                    locator = locator,
                    style = Decoration.Style.Underline(
                        tint = highLightColor.argb,
                        isActive = false,
                    ),
                ),
            )

            ReadAloudHighlightStyle.HIGHLIGHT_UNDERLINE -> listOf(
                Decoration(
                    id = "readaloud-highlight",
                    locator = locator,
                    style = Decoration.Style.Highlight(
                        tint = highLightColor.argb,
                        isActive = false,
                    ),
                ),
                Decoration(
                    id = "readaloud-underline",
                    locator = locator,
                    style = Decoration.Style.Underline(
                        tint = highLightColor.argb,
                        isActive = false,
                    ),
                ),
            )
        }
    }

    override fun close() {
        pendingPageTurnJob?.cancel()
        controllerScope.cancel()
        _navigator.value = null
    }

    private companion object {
        private val logger = Logger.withTag("AndroidEpubNavigatorController")

        /** Minimum delay before page turn to avoid jarring transitions */
        private const val MIN_PAGE_TURN_DELAY_MS = 200L
    }
}

/**
 * Extension function to convert ReaderSettingsUiModel to EpubPreferences.
 * This is used for initial preferences when creating the navigator and for dynamic updates.
 */
fun ReaderSettingsUiModel.toEpubPreferences(): EpubPreferences {
    return EpubPreferences(
        fontSize = fontSize,
        scroll = scrollMode,
        theme = theme.toReadiumTheme(),
        lineHeight = lineHeight.toDouble(),
        pageMargins = calculatePageMargins(),
        textAlign = textAlign.toReadiumTextAlign(),
        publisherStyles = publisherStyles,
    )
}

/**
 * Converts ReaderThemeUi to Readium's Theme.
 * Note: SYSTEM theme is not directly supported by Readium, so we return null to use default.
 */
private fun ReaderThemeUi.toReadiumTheme(): Theme? = when (this) {
    ReaderThemeUi.LIGHT -> Theme.LIGHT
    ReaderThemeUi.DARK -> Theme.DARK
    ReaderThemeUi.SEPIA -> Theme.SEPIA
    ReaderThemeUi.SYSTEM -> null // Let Readium use its default
}

/**
 * Converts ReaderTextAlignUi to Readium's TextAlign.
 */
private fun ReaderTextAlignUi.toReadiumTextAlign(): TextAlign = when (this) {
    ReaderTextAlignUi.START -> TextAlign.START
    ReaderTextAlignUi.END -> TextAlign.END
    ReaderTextAlignUi.CENTER -> TextAlign.CENTER
    ReaderTextAlignUi.JUSTIFY -> TextAlign.JUSTIFY
}

/**
 * Calculates page margins as a factor for Readium.
 * Readium's pageMargins is a multiplier applied to horizontal margins only.
 * We convert our dp-based horizontal margin to a factor based on a 16dp baseline.
 * Vertical margins are applied separately via Compose padding on the reader view.
 */
private fun ReaderSettingsUiModel.calculatePageMargins(): Double {
    // Baseline is 16dp, so 16dp = 1.0 factor
    return (marginHorizontal / 16.0).coerceIn(0.0, 4.0)
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