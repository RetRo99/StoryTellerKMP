package com.retro99.reader.ui.navigator

import android.webkit.JavascriptInterface

/**
 * JavaScript interface for handling double-tap events on sentence elements.
 *
 * This interface is registered with the WebView and called from JavaScript
 * when the user double-taps on a sentence element.
 *
 * @param onDoubleTap Callback invoked with the fragment ID of the double-tapped element
 */
class DoubleTapJsInterface(
    private val onDoubleTap: (String) -> Unit,
) {
    /**
     * Called from JavaScript when a sentence element is double-tapped.
     *
     * @param fragmentId The ID of the element that was double-tapped
     *                   (e.g., "chapter44.xhtml-sentence50")
     */
    @JavascriptInterface
    fun onDoubleTap(fragmentId: String) {
        onDoubleTap.invoke(fragmentId)
    }
}

