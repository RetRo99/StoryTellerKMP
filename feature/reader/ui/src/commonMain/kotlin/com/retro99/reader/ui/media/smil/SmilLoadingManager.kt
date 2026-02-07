package com.retro99.reader.ui.media.smil

import com.retro99.analytics.api.Analytics
import com.retro99.base.nowMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Callback interface for platform-specific SMIL file operations.
 *
 * Since file reading is platform-specific (Readium on Android, Swift on iOS),
 * the platform provides this callback to read SMIL content.
 */
interface SmilContentProvider {
    /**
     * Reads the content of a SMIL file.
     *
     * @param smilHref The href of the SMIL file
     * @return The file content as a string, or null if reading failed
     */
    suspend fun readSmilContent(smilHref: String): String?

    /**
     * Gets all SMIL file hrefs in the publication.
     *
     * @return List of SMIL file hrefs
     */
    fun getAllSmilHrefs(): List<String>

    /**
     * Gets the reading order (list of chapter hrefs in order).
     *
     * @return List of chapter hrefs in reading order
     */
    fun getReadingOrder(): List<String>

    /**
     * Resolves a relative path from a SMIL file to an absolute path.
     *
     * @param smilHref The SMIL file href
     * @param relativePath The relative path from the SMIL file
     * @return The resolved absolute path
     */
    fun resolveSmilPath(smilHref: String, relativePath: String): String
}

/**
 * Manages lazy loading of SMIL files with intelligent prefetching.
 *
 * Features:
 * - Hybrid index building (current + N chapters first, rest deferred)
 * - On-demand parsing with caching
 * - Background prefetching of next chapter
 * - No-eviction cache for session lifetime
 * - Cancelable prefetch when user jumps far
 *
 * @param smilParser Full SMIL parser for extracting clips
 * @param quickScanner Fast scanner for building chapter index
 * @param analytics Analytics for error logging
 * @param ioDispatcher Dispatcher for I/O operations
 */
class SmilLoadingManager(
    private val smilParser: SmilParser,
    private val quickScanner: SmilQuickScanner,
    private val analytics: Analytics,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val index = SmilChapterIndex()
    private val cache = SmilClipCache()

    private var contentProvider: SmilContentProvider? = null
    private var managerScope: CoroutineScope? = null
    private var prefetchJob: Job? = null

    // Number of chapters ahead to scan during initial index build
    private val initialScanAhead = 3

    // Timeout for scanning a single SMIL file (ms)
    private val scanTimeoutMs = 1000L

    // Timeout for parsing a single SMIL file (ms)
    private val parseTimeoutMs = 5000L

    /**
     * Initializes the manager with a content provider and scope.
     *
     * @param provider Platform-specific content provider
     * @param scope Coroutine scope for background operations
     */
    fun initialize(provider: SmilContentProvider, scope: CoroutineScope) {
        contentProvider = provider
        managerScope = CoroutineScope(scope.coroutineContext + SupervisorJob())
    }

    /**
     * Builds the initial SMIL→chapter index for the current chapter and nearby chapters.
     *
     * This is a fast operation that only scans SMIL files until we find the ones
     * for the current chapter + N chapters ahead. Remaining SMIL files are scanned
     * on-demand when their chapters are accessed.
     *
     * @param currentChapterHref The current chapter href
     * @return Time taken in milliseconds
     */
    suspend fun buildInitialIndex(currentChapterHref: String): Long = withContext(ioDispatcher) {
        val startTime = currentTimeMillis()
        val provider = contentProvider ?: return@withContext 0L

        val normalizedCurrent = quickScanner.normalizeChapterHref(currentChapterHref)
        val readingOrder = provider.getReadingOrder().map { quickScanner.normalizeChapterHref(it) }
        val allSmilHrefs = provider.getAllSmilHrefs()

        // Find current chapter position in reading order
        val currentIndex = readingOrder.indexOf(normalizedCurrent).takeIf { it >= 0 } ?: 0

        // Determine which chapters we need to find SMILs for (current + N ahead)
        val chaptersToFind = readingOrder
            .drop(currentIndex)
            .take(initialScanAhead + 1)
            .toMutableSet()

        // Scan SMIL files until we find all the chapters we need
        var scannedCount = 0

        for (smilHref in allSmilHrefs) {
            // Stop early if we found SMILs for all chapters we need
            if (chaptersToFind.isEmpty()) {
                break
            }

            if (index.isScanned(smilHref)) continue

            val chapterHref = scanSmilFile(smilHref)
            scannedCount++

            if (chapterHref != null && chapterHref in chaptersToFind) {
                // Found a SMIL for one of the chapters we need
                // Check if we have at least one SMIL per chapter
                if (index.hasSmilForChapter(chapterHref)) {
                    chaptersToFind.remove(chapterHref)
                }
            }
        }

        currentTimeMillis() - startTime
    }

    /**
     * Scans a single SMIL file to extract its chapter reference.
     *
     * @param smilHref The SMIL file href
     * @return The normalized chapter href, or null if not found
     */
    private suspend fun scanSmilFile(smilHref: String): String? {
        val provider = contentProvider ?: return null

        return try {
            val content = withTimeoutOrNull(scanTimeoutMs) {
                provider.readSmilContent(smilHref)
            }

            if (content == null) {
                index.markFailed(smilHref)
                return null
            }

            val chapterHref = quickScanner.scanForChapterHref(content, smilHref)
            if (chapterHref != null) {
                val normalized = quickScanner.normalizeChapterHref(chapterHref)
                index.registerSmilForChapter(normalized, smilHref)
                normalized
            } else {
                index.markScanned(smilHref)
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            analytics.logException(e, "Failed to scan SMIL file: $smilHref")
            index.markFailed(smilHref)
            null
        }
    }

    /**
     * Gets clips for a chapter, parsing SMIL files if needed.
     *
     * This is the main entry point for getting clips. It will:
     * 1. Return cached clips if available
     * 2. Parse SMIL files for the chapter if not cached
     * 3. Trigger fallback scan if no SMIL files are indexed for this chapter
     *
     * @param chapterHref The chapter href
     * @return List of clips for the chapter
     */
    suspend fun getClipsForChapter(chapterHref: String): List<SmilClip> =
        withContext(ioDispatcher) {
            val normalized = quickScanner.normalizeChapterHref(chapterHref)

            // Check cache first
            cache.getClips(normalized)?.let { cached ->
                return@withContext cached
            }

            // Try to claim parsing work
            if (!cache.tryClaimParsing(normalized)) {
                // Another coroutine is parsing, wait for it
                while (!cache.isParsed(normalized)) {
                    kotlinx.coroutines.delay(10)
                }
                return@withContext cache.getClips(normalized) ?: emptyList()
            }

            // Parse SMIL files for this chapter
            val clips = parseChapterSmilFiles(normalized)
            cache.putClips(normalized, clips)

            clips
        }

    /**
     * Parses all SMIL files for a chapter.
     */
    private suspend fun parseChapterSmilFiles(normalizedChapterHref: String): List<SmilClip> {
        contentProvider ?: return emptyList()

        // Get SMIL files for this chapter from index
        var smilFiles = index.getSmilFilesForChapter(normalizedChapterHref)

        // If no SMIL files indexed, do a fallback scan
        if (smilFiles.isEmpty()) {
            smilFiles = fallbackScanForChapter(normalizedChapterHref)
        }

        if (smilFiles.isEmpty()) {
            return emptyList()
        }

        // Parse each SMIL file
        val allClips = mutableListOf<SmilClip>()
        for (smilHref in smilFiles) {
            val clips = parseSmilFile(smilHref)
            allClips.addAll(clips)
        }

        return allClips.sortedBy { it.clipBegin }
    }

    /**
     * Fallback scan: searches all unscanned SMIL files for ones referencing this chapter.
     */
    private suspend fun fallbackScanForChapter(normalizedChapterHref: String): List<String> {
        val provider = contentProvider ?: return emptyList()
        val allSmilHrefs = provider.getAllSmilHrefs()
        val unscanned = index.getUnscannedSmilFiles(allSmilHrefs)

        for (smilHref in unscanned) {
            scanSmilFile(smilHref)
        }

        return index.getSmilFilesForChapter(normalizedChapterHref)
    }

    /**
     * Parses a single SMIL file and returns its clips.
     */
    private suspend fun parseSmilFile(smilHref: String): List<SmilClip> {
        val provider = contentProvider ?: return emptyList()

        return try {
            val content = withTimeoutOrNull(parseTimeoutMs) {
                provider.readSmilContent(smilHref)
            } ?: return emptyList()

            smilParser.parseClips(content)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            analytics.logException(e, "Failed to parse SMIL file: $smilHref")
            emptyList()
        }
    }

    /**
     * Prefetches clips for the next chapter in background.
     *
     * This should be called after preparing the current chapter.
     * The prefetch is cancelable if the user navigates away.
     *
     * @param currentChapterHref The current chapter href
     */
    fun prefetchNextChapter(currentChapterHref: String) {
        val provider = contentProvider ?: return
        val scope = managerScope ?: return

        // Cancel any existing prefetch
        prefetchJob?.cancel()

        prefetchJob = scope.launch(ioDispatcher) {
            try {
                val normalized = quickScanner.normalizeChapterHref(currentChapterHref)
                val readingOrder = provider.getReadingOrder()
                    .map { quickScanner.normalizeChapterHref(it) }

                val currentIndex = readingOrder.indexOf(normalized)
                if (currentIndex < 0 || currentIndex >= readingOrder.size - 1) {
                    return@launch
                }

                val nextChapter = readingOrder[currentIndex + 1]

                // Skip if already cached
                if (cache.isParsed(nextChapter)) {
                    return@launch
                }

                getClipsForChapter(nextChapter)
            } catch (e: CancellationException) {
                // Prefetch was cancelled, this is expected
            } catch (e: Exception) {
                analytics.logException(e, "Prefetch failed for chapter after: $currentChapterHref")
            }
        }
    }

    /**
     * Gets all cached clips across all parsed chapters.
     *
     * @return All cached clips
     */
    suspend fun getAllCachedClips(): List<SmilClip> = cache.getAllClips()

    /**
     * Gets diagnostics information.
     *
     * @return Diagnostic string with cache and index stats
     */
    suspend fun getDiagnostics(): String {
        val (parsedChapters, totalClips) = cache.getStats()
        val (indexedChapters, scannedSmils, failedSmils) = index.getStats()
        return "Cache: $parsedChapters chapters, $totalClips clips | " +
                "Index: $indexedChapters chapters, $scannedSmils scanned, $failedSmils failed"
    }

    /**
     * Releases all resources and clears caches.
     */
    fun release() {
        prefetchJob?.cancel()
        prefetchJob = null
        managerScope?.cancel()
        managerScope = null
        contentProvider = null
        cache.clear()
        index.clear()
    }

    private fun currentTimeMillis(): Long = nowMillis()
}
