package com.retro99.reader.ui.navigator

import co.touchlab.kermit.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Utility for detecting the first visible sentence element in the EPUB WebView viewport.
 *
 * This is used to get precise audio positioning when the user manually navigates
 * to a different page/chapter. By finding the first visible sentence, we can
 * start audio playback from the exact position the user is viewing.
 *
 * Sentence elements are identified by having IDs (e.g., "chapter44.xhtml-sentence50").
 */
object VisibleSentenceDetector {

    private val logger = Logger.withTag("VisibleSentenceDetector")
    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * JavaScript to find the first visible sentence element in the viewport.
     *
     * This script:
     * 1. Gets all elements with an ID attribute (sentence elements have IDs)
     * 2. Filters to elements that are within the visible viewport
     * 3. Returns the ID of the topmost visible element
     *
     * In paginated EPUB mode, elements on the next virtual page have their
     * left edge beyond the viewport width, so we filter those out.
     */
    private const val VISIBLE_SENTENCE_JS = """
        (function() {
            const vw = window.innerWidth;
            const vh = window.innerHeight;
            
            // Get all elements with an ID - these are potential sentence elements
            const elementsWithId = document.querySelectorAll('[id]');
            
            let topMostElement = null;
            let topMostY = Infinity;
            
            for (let i = 0; i < elementsWithId.length; i++) {
                const el = elementsWithId[i];
                const id = el.id;
                
                // Skip elements without meaningful IDs or non-sentence elements
                if (!id || id.length === 0) continue;
                
                const rects = el.getClientRects();
                if (rects.length === 0) continue;
                
                // Get the first rect (first line of the element)
                const rect = rects[0];
                
                // Check if the element is within the visible viewport
                // In paginated mode, elements on next page have left >= vw
                const isHorizontallyVisible = rect.left >= 0 && rect.left < vw;
                const isVerticallyVisible = rect.top >= 0 && rect.top < vh;
                
                if (isHorizontallyVisible && isVerticallyVisible) {
                    // Track the topmost visible element
                    if (rect.top < topMostY) {
                        topMostY = rect.top;
                        topMostElement = id;
                    }
                }
            }
            
            if (topMostElement) {
                return JSON.stringify({
                    status: 'found',
                    elementId: topMostElement,
                    topY: topMostY
                });
            } else {
                return JSON.stringify({ status: 'not_found' });
            }
        })()
    """

    /**
     * Gets the JavaScript for finding the first visible sentence element.
     *
     * @return The JavaScript code ready to execute
     */
    fun getScript(): String = VISIBLE_SENTENCE_JS.trimIndent()

    /**
     * Parses the JSON result from the visible sentence detection JavaScript.
     *
     * @param json The raw JSON string from JavaScript evaluation
     * @return The element ID of the first visible sentence, or null if not found
     */
    fun parseResult(json: String): String? {
        return try {
            val data = jsonParser.decodeFromString<VisibleSentenceResult>(json)
            if (data.status == "found") {
                logger.d { "Found visible sentence: ${data.elementId} at y=${data.topY}" }
                data.elementId
            } else {
                logger.d { "No visible sentence found" }
                null
            }
        } catch (e: Exception) {
            logger.w(e) { "Failed to parse visible sentence result: $json" }
            null
        }
    }
}

/**
 * Data class for parsing the visible sentence detection JavaScript result.
 */
@Serializable
internal data class VisibleSentenceResult(
    @SerialName("status")
    val status: String,
    @SerialName("elementId")
    val elementId: String? = null,
    @SerialName("topY")
    val topY: Double? = null,
)

