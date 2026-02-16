package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.model.ChapterInfo
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.TextAlign
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

/** Decoration group name for ReadAloud text highlighting */
private const val READALOUD_DECORATION_GROUP = "readaloud"

/**
 * Android implementation of [BookController] using Readium's EpubNavigatorFragment.
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
class AndroidBookController internal constructor() : BookController {

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

    /**
     * Cache for chapter word counts, keyed by chapter href.
     * Word count is static per chapter, so we only fetch it once per chapter.
     */
    private val chapterWordCountCache = mutableMapOf<String, Int>()

    /**
     * Cancels any pending page turn and resets the tracking state.
     */
    private fun cancelPendingPageTurn() {
        pendingPageTurnJob?.cancel()
        lastPageTurnSentenceId = null
    }

    /**
     * SharedFlow for emitting double-tap events on sentence elements.
     * Uses replay=1 to ensure late subscribers receive the most recent event.
     */
    private val _sentenceDoubleTapEvents = MutableSharedFlow<SentenceDoubleTapEvent>(replay = 1)

    /**
     * Flow of double-tap events on sentence elements.
     * Emits when the user double-taps on a sentence in the EPUB content.
     */
    override val sentenceDoubleTapEvents: Flow<SentenceDoubleTapEvent> =
        _sentenceDoubleTapEvents.asSharedFlow()

    /**
     * Whether this book has media overlays (ReadAloud capability).
     * Used to determine if double-tap detection should be enabled.
     */
    override var hasMediaOverlays: Boolean = false
        private set

    private val navigator: EpubNavigatorFragment
        get() = _navigator.value ?: error("Navigator not initialized")

    fun init(
        navigator: EpubNavigatorFragment,
        hasMediaOverlays: Boolean = false,
    ) {
        _navigator.value = navigator
        this.hasMediaOverlays = hasMediaOverlays
        // Only inject double-tap detection script for ReadAloud books
        if (hasMediaOverlays) {
            injectDoubleTapDetectionScript()
        }
    }

    /**
     * Injects the double-tap detection JavaScript into the navigator's WebView.
     * This script listens for double-click events and calls back to native code.
     *
     * Uses a small delay to ensure the WebView content is loaded.
     * The script has built-in protection against multiple injections.
     */
    private fun injectDoubleTapDetectionScript() {
        controllerScope.launch {
            // Small delay to ensure WebView content is loaded
            delay(SCRIPT_INJECTION_DELAY_MS)
            val script = DoubleTapDetector.getDoubleTapDetectionScript("SentenceDoubleTap")
            _navigator.value?.evaluateJavascript(script)
        }
    }

    /**
     * Called from JavaScript when a sentence is double-tapped.
     * This method is invoked via the JavaScript interface from a background thread.
     * We launch a coroutine on Main dispatcher to safely access navigator state.
     */
    fun onSentenceDoubleTap(fragmentId: String) {
        controllerScope.launch {
            // Access navigator state on Main thread for thread safety
            val currentHref = _navigator.value?.currentLocator?.value?.href?.toString()
            _sentenceDoubleTapEvents.emit(
                SentenceDoubleTapEvent(
                    fragmentId = fragmentId,
                    chapterHref = currentHref,
                )
            )
        }
    }

    /**
     * Flow of current reading position/locator changes.
     * Converts Readium's Locator to the common LocatorState model.
     * Enriches the state with chapter info (page position and cached word count).
     * Delays the first emission by 500ms.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentLocator: Flow<LocatorState> = _navigator.flatMapLatest { navigator ->
        navigator?.currentLocator?.map { locator ->
            val href = locator.href.toString()

            // Get or fetch chapter word count (cached per chapter)
            val cachedWordCount = getOrFetchChapterWordCount(href)

            // Fetch chapter info with page position and word count
            val chapterInfo = fetchChapterInfo(cachedWordCount)

            LocatorState(
                href = href,
                type = locator.mediaType.toString(),
                title = locator.title,
                progression = locator.locations.progression,
                position = locator.locations.position,
                totalProgression = locator.locations.totalProgression,
                fragments = locator.locations.fragments,
                chapterInfo = chapterInfo,
            )
        } ?: flowOf()
    }.onStart {
        delay(500)
    }.onEach {
        cancelPendingPageTurn()
    }

    /**
     * Fetches the chapter info from the WebView.
     * This is called on every locator change since page info depends on scroll position.
     * Word count is passed in from the cache.
     */
    private suspend fun fetchChapterInfo(cachedWordCount: Int?): ChapterInfo? {
        val nav = _navigator.value ?: return null
        val script = ChapterPageCalculator.getPageCalculationScript()
        val rawResult = nav.evaluateJavascript(script) ?: return null
        val cleanJson = cleanWebViewJson(rawResult)
        return ChapterPageCalculator.parsePageResult(cleanJson, cachedWordCount)
    }

    /**
     * Gets the cached word count for a chapter, or fetches it if not cached.
     * Word count is static per chapter, so we only fetch it once.
     */
    private suspend fun getOrFetchChapterWordCount(href: String): Int? {
        // Return cached value if available
        chapterWordCountCache[href]?.let { return it }

        // Fetch and cache the word count
        val nav = _navigator.value ?: return null
        val script = ChapterWordCountCalculator.getWordCountScript()
        val rawResult = nav.evaluateJavascript(script) ?: return null
        val cleanJson = cleanWebViewJson(rawResult)
        val wordCount = ChapterWordCountCalculator.parseWordCountResult(cleanJson)

        // Cache the result
        if (wordCount != null) {
            chapterWordCountCache[href] = wordCount
        }

        return wordCount
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
     * 1. Navigate to the locator (ensuring the sentence is visible)
     * 2. Apply the highlight decoration
     * 3. Check if the sentence spans pages (partially visible)
     * 4. Schedule a page turn after the visible portion has been read
     */
    override suspend fun applyHighlightWithPageTurn(
        locator: LocatorState,
        sentenceDurationMs: Long,
    ) {
        val decorableNavigator = _navigator.value as? DecorableNavigator ?: return
        val androidLocator = locator.toAndroidLocator() ?: return
        val fragmentId = locator.fragments?.firstOrNull()

        // First navigate to the locator to ensure the sentence is visible
        navigator.go(androidLocator)

        // Apply highlight decorations
        val decorations = createDecorations(androidLocator)
        decorableNavigator.applyDecorations(decorations, READALOUD_DECORATION_GROUP)

        // Cancel any pending page turn from a previous sentence
        cancelPendingPageTurn()

        // Check visibility and schedule page turn if sentence spans pages
        if (fragmentId != null) {
            val visibility = checkSentenceVisibility(fragmentId)
            if (visibility.needsPageTurn) {
                // Calculate delay based on visible fraction
                val delayMs = (visibility.visibleFraction * sentenceDurationMs).toLong()
                    .coerceAtLeast(MIN_PAGE_TURN_DELAY_MS)

                lastPageTurnSentenceId = fragmentId
                pendingPageTurnJob = controllerScope.launch {
                    delay(delayMs)
                    navigator.goForward()
                }
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

    override suspend fun getChapterPageInfo(): ChapterInfo? {
        // For manual refresh, we don't have the href, so we can't include cached word count
        // This is fine since this method is mainly used for page info after settings changes
        return fetchChapterInfo(cachedWordCount = null)
    }

    override suspend fun getVisibleSentenceId(): String? {
        val nav = _navigator.value ?: return null

        val script = VisibleSentenceDetector.getScript()
        val rawResult = nav.evaluateJavascript(script) ?: return null

        val cleanJson = cleanWebViewJson(rawResult)
        return VisibleSentenceDetector.parseResult(cleanJson)
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
        // Note: No need to call getRemoveDoubleTapDetectorScript() here.
        // The WebView and its JavaScript context will be destroyed when the
        // fragment is removed, so the event listener will be cleaned up automatically.
        controllerScope.cancel()
        _navigator.value = null
    }

    private companion object Companion {
        /** Minimum delay before page turn to avoid jarring transitions */
        private const val MIN_PAGE_TURN_DELAY_MS = 200L

        /** Delay before injecting double-tap detection script to ensure WebView is ready */
        private const val SCRIPT_INJECTION_DELAY_MS = 500L
    }
}

/**
 * Extension function to convert ReaderSettingsUiModel to EpubPreferences.
 * This is used for initial preferences when creating the navigator and for dynamic updates.
 */
fun ReaderSettingsUiModel.toEpubPreferences(): EpubPreferences {
    return EpubPreferences(
        fontSize = fontSize,
        fontFamily = fontFamily.toReadiumFontFamily(),
        scroll = scrollMode,
        theme = theme.toReadiumTheme(),
        lineHeight = lineHeight.toDouble(),
        pageMargins = calculatePageMargins(),
        textAlign = textAlign.toReadiumTextAlign(),
        publisherStyles = publisherStyles,
    )
}

/**
 * Converts font family string to Readium's FontFamily.
 * Returns null for "default" to use publisher's font.
 */
private fun String.toReadiumFontFamily(): FontFamily? = when (this) {
    "default" -> null
    else -> FontFamily(this)
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