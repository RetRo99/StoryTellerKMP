package com.retro99.reader.ui.navigator

import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.model.ChapterPageInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Shared utility for calculating the current page within a chapter.
 *
 * Uses JavaScript to determine the current page based on the actual viewport display.
 * In paginated EPUB mode, this calculates pages based on scroll position and viewport width.
 *
 * This provides a more meaningful page number than the EPUB position, which is based on
 * fixed 1024-character blocks and doesn't change with font size or viewport dimensions.
 */
object ChapterPageCalculator : KoinComponent {

    private val analytics: Analytics by inject<Analytics>()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * JavaScript to calculate the current page within the chapter.
     *
     * Readium renders EPUB content inside an iframe. The JavaScript is executed
     * inside that iframe context, so we're already in the content document.
     *
     * In paginated mode, Readium uses CSS columns with a fixed height container.
     * The content overflows horizontally and is scrolled via scrollLeft.
     *
     * Returns JSON with currentPage (1-based) and totalPages.
     */
    private const val PAGE_CALCULATION_JS = """
        (function() {
            try {
                const vw = window.innerWidth;

                // In Readium, the content is in a scrollable container
                const html = document.documentElement;
                const body = document.body;

                // Get scroll position - check both html and body
                const htmlScrollLeft = html ? html.scrollLeft : 0;
                const bodyScrollLeft = body ? body.scrollLeft : 0;
                const windowScrollX = window.scrollX || window.pageXOffset || 0;
                const scrollX = Math.max(htmlScrollLeft, bodyScrollLeft, windowScrollX);

                // Get total scrollable width
                const htmlScrollWidth = html ? html.scrollWidth : 0;
                const bodyScrollWidth = body ? body.scrollWidth : 0;
                const totalWidth = Math.max(htmlScrollWidth, bodyScrollWidth);

                if (vw <= 0 || totalWidth <= 0) {
                    return JSON.stringify({ status: 'error', message: 'Invalid dimensions' });
                }

                const totalPages = Math.max(1, Math.ceil(totalWidth / vw));
                const currentPage = Math.min(totalPages, Math.floor(scrollX / vw) + 1);

                return JSON.stringify({
                    status: 'success',
                    currentPage: currentPage,
                    totalPages: totalPages
                });
            } catch (e) {
                return JSON.stringify({ status: 'error', message: e.message });
            }
        })()
    """

    /**
     * Gets the JavaScript for calculating the current chapter page.
     *
     * @return The JavaScript code ready to execute
     */
    fun getPageCalculationScript(): String = PAGE_CALCULATION_JS

    /**
     * Parses the JSON result from the page calculation JavaScript.
     *
     * @param json The raw JSON string from JavaScript evaluation
     * @return The parsed chapter page info, or null on error
     */
    fun parsePageResult(json: String): ChapterPageInfo? {
        return try {
            parsePageResultInternal(json)
        } catch (e: Exception) {
            analytics.logException(e, "Failed to parse page result, json: $json")
            null
        }
    }

    private fun parsePageResultInternal(json: String): ChapterPageInfo? {
        val data = jsonParser.decodeFromString<PageCalculationResult>(json)

        if (data.status != "success") {
            return null
        }

        val currentPage = data.currentPage ?: return null
        val totalPages = data.totalPages ?: return null

        return ChapterPageInfo(
            currentPage = currentPage,
            totalPages = (totalPages - 1).coerceAtLeast(1)
        )
    }
}

/**
 * Data class for parsing the page calculation JavaScript result.
 */
@Serializable
internal data class PageCalculationResult(
    @SerialName("status")
    val status: String,
    @SerialName("currentPage")
    val currentPage: Int? = null,
    @SerialName("totalPages")
    val totalPages: Int? = null,
    @SerialName("message")
    val message: String? = null,
)

