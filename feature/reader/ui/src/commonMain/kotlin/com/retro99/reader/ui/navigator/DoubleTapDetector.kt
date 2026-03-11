package com.retro99.reader.ui.navigator

/**
 * Utility for detecting taps on sentence elements in the EPUB WebView.
 *
 * This injects JavaScript that:
 * 1. Listens for click events on the document
 * 2. Finds the closest element with an ID (sentence elements have IDs like "chapter44.xhtml-sentence50")
 * 3. Calls a native callback with the fragment ID
 *
 * Double-tap detection is handled in native code (ReaderGesturesModifier) for consistent
 * timing control. This script just reports each tap with the element ID.
 *
 * The callback is registered via a JavaScript interface on Android or message handler on iOS.
 */
object DoubleTapDetector {

    /**
     * JavaScript code to inject for tap detection on sentence elements.
     *
     * This script:
     * - Listens for 'click' events (single taps)
     * - Traverses up the DOM to find the nearest element with an ID
     * - Calls a native callback with the fragment ID
     *
     * Native code is responsible for double-tap detection timing.
     *
     * The callback name is configurable to support different platforms:
     * - Android: Uses JavaScriptInterface with a specific method name
     * - iOS: Uses WKScriptMessageHandler
     *
     * @param callbackName The name of the native callback function to invoke
     */
    fun getTapDetectionScript(callbackName: String = "SentenceTap"): String {
        return """
            (function() {
                // Prevent multiple injections
                if (window.__tapDetectorInstalled) return;
                window.__tapDetectorInstalled = true;

                document.addEventListener('click', function(event) {
                    // Find the closest element with an ID
                    var element = event.target;
                    while (element && element !== document.body) {
                        if (element.id) {
                            // Found an element with an ID - this is likely a sentence
                            try {
                                if (typeof $callbackName !== 'undefined' && $callbackName.onTap) {
                                    $callbackName.onTap(element.id);
                                } else if (window.webkit && window.webkit.messageHandlers &&
                                           window.webkit.messageHandlers.$callbackName) {
                                    window.webkit.messageHandlers.$callbackName.postMessage(element.id);
                                }
                            } catch (e) {
                                console.error('Tap callback error:', e);
                            }
                            return;
                        }
                        element = element.parentElement;
                    }
                    // No element with ID found - report empty string
                    try {
                        if (typeof $callbackName !== 'undefined' && $callbackName.onTap) {
                            $callbackName.onTap('');
                        } else if (window.webkit && window.webkit.messageHandlers &&
                                   window.webkit.messageHandlers.$callbackName) {
                            window.webkit.messageHandlers.$callbackName.postMessage('');
                        }
                    } catch (e) {
                        console.error('Tap callback error:', e);
                    }
                }, { passive: true });
            })();
        """.trimIndent()
    }

    /**
     * JavaScript code to remove the tap detector.
     * Call this when cleaning up the WebView.
     */
    fun getRemoveTapDetectorScript(): String {
        return """
            (function() {
                window.__tapDetectorInstalled = false;
            })();
        """.trimIndent()
    }
}

