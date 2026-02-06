package com.retro99.reader.ui.media.smil

import org.koin.core.annotation.Single

/**
 * Fast scanner for extracting chapter references from SMIL files.
 *
 * This scanner uses regex to quickly find the first textSrc reference
 * without fully parsing the XML. This is much faster than full parsing
 * and sufficient for building the SMIL→chapter index.
 *
 * Typical SMIL structure:
 * ```xml
 * <smil>
 *   <body>
 *     <seq>
 *       <par>
 *         <text src="chapter1.xhtml#s1"/>
 *         <audio src="audio.mp3" clipBegin="0s" clipEnd="2.5s"/>
 *       </par>
 *     </seq>
 *   </body>
 * </smil>
 * ```
 */
@Single
class SmilQuickScanner {

    // Regex to find the first text src attribute
    // Matches: <text src="..." or <text ... src="..."
    // Captures the src value
    private val textSrcRegex = Regex(
        pattern = """<text[^>]*\ssrc\s*=\s*["']([^"']+)["']""",
        option = RegexOption.IGNORE_CASE,
    )

    /**
     * Quickly scans SMIL content to extract the chapter href it references.
     *
     * This method finds the first <text src="..."> element and extracts
     * the chapter href (removing any fragment identifier).
     *
     * @param content The SMIL file content
     * @param smilHref The href of the SMIL file (for resolving relative paths)
     * @return The normalized chapter href, or null if not found
     */
    fun scanForChapterHref(content: String, smilHref: String): String? {
        if (content.isBlank()) return null

        // Find the first text src
        val match = textSrcRegex.find(content) ?: return null
        val textSrc = match.groupValues.getOrNull(1) ?: return null

        if (textSrc.isBlank()) return null

        // Remove fragment (everything after #)
        val hrefWithoutFragment = textSrc.substringBefore('#')

        // Resolve relative path against SMIL file location
        return resolveRelativePath(smilHref, hrefWithoutFragment)
    }

    /**
     * Resolves a relative path against a base path.
     *
     * Examples:
     * - base: "OEBPS/smil/chapter1.smil", relative: "../Text/chapter1.xhtml"
     *   → "OEBPS/Text/chapter1.xhtml"
     * - base: "chapter1.smil", relative: "chapter1.xhtml"
     *   → "chapter1.xhtml"
     */
    private fun resolveRelativePath(basePath: String, relativePath: String): String {
        // If relative path is absolute (starts with /), return as-is
        if (relativePath.startsWith('/')) {
            return relativePath.removePrefix("/")
        }

        // Get the directory of the base path
        val baseDir = basePath.substringBeforeLast('/', "")

        // Handle ../ navigation
        var currentDir = baseDir
        var remainingPath = relativePath

        while (remainingPath.startsWith("../")) {
            remainingPath = remainingPath.removePrefix("../")
            currentDir = currentDir.substringBeforeLast('/', "")
        }

        // Remove leading ./ if present
        remainingPath = remainingPath.removePrefix("./")

        // Combine directory and remaining path
        return if (currentDir.isEmpty()) {
            remainingPath
        } else {
            "$currentDir/$remainingPath"
        }
    }

    /**
     * Normalizes a chapter href for consistent comparison.
     *
     * - Removes fragment identifiers
     * - Removes leading slashes
     * - Normalizes path separators
     *
     * @param href The href to normalize
     * @return Normalized href
     */
    fun normalizeChapterHref(href: String): String {
        return href
            .substringBefore('#')
            .removePrefix("/")
            .replace("\\", "/")
    }
}

