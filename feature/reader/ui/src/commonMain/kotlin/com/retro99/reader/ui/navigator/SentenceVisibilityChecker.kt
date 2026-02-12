package com.retro99.reader.ui.navigator

import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.navigator.SentenceVisibilityChecker.getVisibilityCheckScript
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Shared utility for checking sentence visibility in paginated EPUB views.
 *
 * Uses JavaScript's `getClientRects()` to determine how much of a sentence element
 * is visible on the current page. In paginated EPUB mode, lines that are on the
 * next virtual page will have their left edge beyond the viewport width.
 *
 * This is used for pre-emptive page turn logic during TTS playback.
 */
object SentenceVisibilityChecker : KoinComponent {

    private val analytics: Analytics by inject<Analytics>()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /** Minimum visible fraction required to NOT trigger a page turn */
    const val MINIMUM_VISIBLE_FRACTION = 0.8

    /** Threshold for "awkward buffer" - last 10% of page width */
    const val AWKWARD_BUFFER_THRESHOLD = 0.90

    /**
     * JavaScript to check sentence visibility using getClientRects().
     *
     * In paginated EPUB mode, lines on the next virtual page have their
     * left edge beyond the viewport width.
     *
     * Use with String.format() or String.replace("%s", elementId).
     *
     * Note: Android uses `%%` for literal `%` in format strings, but iOS uses `%`.
     * Use [getVisibilityCheckScript] to get the properly formatted script.
     */
    private const val VISIBILITY_CHECK_JS_TEMPLATE = """
        (function() {
            const el = document.getElementById('%s');
            if (!el) return JSON.stringify({ status: 'not_found' });

            const rects = el.getClientRects();
            if (rects.length === 0) return JSON.stringify({ status: 'not_found' });

            const vw = window.innerWidth;

            let linesOffScreen = 0;
            for (let i = 0; i < rects.length; i++) {
                const rect = rects[i];
                // Check if line is off-screen to the right (next page)
                // OR off-screen to the left (previous page - right edge <= 0)
                if (rect.left >= (vw - 1) || rect.right <= 0) {
                    linesOffScreen++;
                }
            }

            return JSON.stringify({
                status: 'found',
                totalLines: rects.length,
                linesOffScreen: linesOffScreen,
                firstCharOffset: rects[0].left MODULO_PLACEHOLDER vw,
                vw: vw
            });
        })()
    """

    /**
     * Gets the JavaScript for checking visibility of a specific element.
     *
     * @param elementId The ID of the element to check
     * @param useDoublePercent If true, uses `%%` for modulo (Android format strings).
     *                         If false, uses `%` (iOS/direct replacement).
     * @return The JavaScript code ready to execute
     */
    fun getVisibilityCheckScript(elementId: String, useDoublePercent: Boolean = false): String {
        val modulo = if (useDoublePercent) "%%" else "%"
        return VISIBILITY_CHECK_JS_TEMPLATE
            .replace("MODULO_PLACEHOLDER", modulo)
            .replace("%s", elementId)
    }

    /**
     * Parses the JSON result from the visibility check JavaScript.
     *
     * @param json The raw JSON string from JavaScript evaluation
     * @param elementId The element ID (for logging purposes)
     * @return The parsed visibility result, or [SentenceVisibilityResult.FULLY_VISIBLE] on error
     */
    fun parseVisibilityResult(json: String, elementId: String): SentenceVisibilityResult {
        return try {
            parseVisibilityResultInternal(json)
        } catch (e: Exception) {
            analytics.logException(
                e,
                "Failed to parse visibility result for element: $elementId, json: $json",
            )
            SentenceVisibilityResult.FULLY_VISIBLE
        }
    }

    private fun parseVisibilityResultInternal(json: String): SentenceVisibilityResult {
        val data = jsonParser.decodeFromString<VisibilityCheckResult>(json)

        if (data.status == "not_found") {
            return SentenceVisibilityResult.HIDDEN
        }

        val totalLines = data.totalLines ?: return SentenceVisibilityResult.FULLY_VISIBLE
        val linesOff = data.linesOffScreen ?: 0
        val firstCharOffset = data.firstCharOffset ?: 0.0
        val vw = data.vw ?: return SentenceVisibilityResult.FULLY_VISIBLE

        // Guard against division by zero
        if (totalLines <= 0) {
            return SentenceVisibilityResult.FULLY_VISIBLE
        }

        // Calculate visible fraction based on line count
        val visibleFraction = (totalLines - linesOff).toDouble() / totalLines

        // Check if sentence starts in the "awkward buffer" (last 10% of page width)
        val startsInAwkwardBuffer = firstCharOffset > (vw * AWKWARD_BUFFER_THRESHOLD)

        // Determine if page turn is needed
        val needsPageTurn = (visibleFraction <= 0.0) ||
                (visibleFraction < MINIMUM_VISIBLE_FRACTION) ||
                startsInAwkwardBuffer

        return SentenceVisibilityResult(
            visibleFraction = visibleFraction,
            needsPageTurn = needsPageTurn,
        )
    }
}

/**
 * Data class for parsing the visibility check JavaScript result.
 */
@Serializable
internal data class VisibilityCheckResult(
    @SerialName("status")
    val status: String,
    @SerialName("totalLines")
    val totalLines: Int? = null,
    @SerialName("linesOffScreen")
    val linesOffScreen: Int? = null,
    @SerialName("firstCharOffset")
    val firstCharOffset: Double? = null,
    @SerialName("vw")
    val vw: Double? = null,
)

