package com.retro99.reader.ui.navigator

import android.webkit.JavascriptInterface

/**
 * JavaScript interface for handling tap events on sentence elements.
 *
 * This interface is registered with the WebView and called from JavaScript
 * when the user taps on a sentence element. Double-tap detection is handled
 * natively in the controller for consistent timing control.
 *
 * @param onTap Callback invoked with the fragment ID of the tapped element
 */
class SentenceTapJsInterface(
    private val onTap: (String) -> Unit,
) {
    /**
     * Called from JavaScript when a sentence element is tapped.
     *
     * @param fragmentId The ID of the element that was tapped
     *                   (e.g., "chapter44.xhtml-sentence50"), or empty string if no element found
     */
    @JavascriptInterface
    fun onTap(fragmentId: String) {
        onTap.invoke(fragmentId)
    }
}

