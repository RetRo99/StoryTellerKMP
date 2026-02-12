package com.retro99.reader.ui.navigator

/**
 * Utility for detecting double-taps on sentence elements in the EPUB WebView.
 *
 * This injects JavaScript that:
 * 1. Listens for double-tap/double-click events on the document
 * 2. Finds the closest element with an ID (sentence elements have IDs like "chapter44.xhtml-sentence50")
 * 3. Calls a native callback with the fragment ID
 *
 * The callback is registered via a JavaScript interface on Android or message handler on iOS.
 */
object DoubleTapDetector {

    /**
     * JavaScript code to inject for double-tap detection.
     *
     * This script:
     * - Listens for 'dblclick' events (works for both mouse and touch double-taps)
     * - Traverses up the DOM to find the nearest element with an ID
     * - Calls the native callback with the fragment ID
     *
     * The callback name is configurable to support different platforms:
     * - Android: Uses JavaScriptInterface with a specific method name
     * - iOS: Uses WKScriptMessageHandler
     *
     * @param callbackName The name of the native callback function to invoke
     */
    fun getDoubleTapDetectionScript(callbackName: String = "onSentenceDoubleTap"): String {
        return """
            (function() {
                // Prevent multiple injections
                if (window.__doubleTapDetectorInstalled) return;
                window.__doubleTapDetectorInstalled = true;
                
                document.addEventListener('dblclick', function(event) {
                    // Find the closest element with an ID
                    var element = event.target;
                    while (element && element !== document.body) {
                        if (element.id) {
                            // Found an element with an ID - this is likely a sentence
                            try {
                                if (typeof $callbackName !== 'undefined' && $callbackName.onDoubleTap) {
                                    $callbackName.onDoubleTap(element.id);
                                } else if (window.webkit && window.webkit.messageHandlers && 
                                           window.webkit.messageHandlers.$callbackName) {
                                    window.webkit.messageHandlers.$callbackName.postMessage(element.id);
                                }
                            } catch (e) {
                                console.error('Double-tap callback error:', e);
                            }
                            return;
                        }
                        element = element.parentElement;
                    }
                }, { passive: true });
            })();
        """.trimIndent()
    }

    /**
     * JavaScript code to remove the double-tap detector.
     * Call this when cleaning up the WebView.
     */
    fun getRemoveDoubleTapDetectorScript(): String {
        return """
            (function() {
                window.__doubleTapDetectorInstalled = false;
            })();
        """.trimIndent()
    }
}

