package com.retro99.reader.ui.media.smil

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Lightweight index mapping chapters to their SMIL files.
 *
 * This index is built incrementally by scanning SMIL files for their
 * first textSrc reference. The scan is fast because it stops as soon
 * as the chapter reference is found.
 *
 * Structure: chapterHref → list of smilHref (some chapters may have multiple SMILs)
 */
class SmilChapterIndex {

    private val mutex = Mutex()

    // chapterHref (normalized, no fragment) → list of smilHrefs
    private val chapterToSmilMap = mutableMapOf<String, MutableList<String>>()

    // Set of SMIL files that have been scanned
    private val scannedSmilFiles = mutableSetOf<String>()

    // Set of SMIL files that failed to scan (to avoid retrying)
    private val failedSmilFiles = mutableSetOf<String>()

    /**
     * Registers a SMIL file as belonging to a chapter.
     *
     * @param chapterHref Normalized chapter href (without fragment)
     * @param smilHref The SMIL file href
     */
    suspend fun registerSmilForChapter(chapterHref: String, smilHref: String) {
        mutex.withLock {
            val smilList = chapterToSmilMap.getOrPut(chapterHref) { mutableListOf() }
            if (smilHref !in smilList) {
                smilList.add(smilHref)
            }
            scannedSmilFiles.add(smilHref)
        }
    }

    /**
     * Marks a SMIL file as scanned (even if no chapter was found).
     *
     * @param smilHref The SMIL file href
     */
    suspend fun markScanned(smilHref: String) {
        mutex.withLock {
            scannedSmilFiles.add(smilHref)
        }
    }

    /**
     * Marks a SMIL file as failed (to avoid retrying).
     *
     * @param smilHref The SMIL file href
     */
    suspend fun markFailed(smilHref: String) {
        mutex.withLock {
            failedSmilFiles.add(smilHref)
            scannedSmilFiles.add(smilHref)
        }
    }

    /**
     * Checks if a SMIL file has been scanned.
     *
     * @param smilHref The SMIL file href
     * @return true if already scanned
     */
    suspend fun isScanned(smilHref: String): Boolean = mutex.withLock {
        smilHref in scannedSmilFiles
    }

    /**
     * Gets the list of SMIL files for a chapter.
     *
     * @param chapterHref Normalized chapter href (without fragment)
     * @return List of SMIL file hrefs, or empty if none found
     */
    suspend fun getSmilFilesForChapter(chapterHref: String): List<String> = mutex.withLock {
        chapterToSmilMap[chapterHref]?.toList() ?: emptyList()
    }

    /**
     * Checks if a chapter has any known SMIL files.
     *
     * @param chapterHref Normalized chapter href (without fragment)
     * @return true if at least one SMIL file is mapped
     */
    suspend fun hasSmilForChapter(chapterHref: String): Boolean = mutex.withLock {
        chapterToSmilMap[chapterHref]?.isNotEmpty() == true
    }

    /**
     * Gets all SMIL files that haven't been scanned yet.
     *
     * @param allSmilFiles Complete list of SMIL file hrefs in the publication
     * @return List of unscanned SMIL file hrefs
     */
    suspend fun getUnscannedSmilFiles(allSmilFiles: List<String>): List<String> = mutex.withLock {
        allSmilFiles.filter { it !in scannedSmilFiles }
    }

    /**
     * Gets index statistics for diagnostics.
     *
     * @return Triple of (chapters indexed, SMIL files scanned, SMIL files failed)
     */
    suspend fun getStats(): Triple<Int, Int, Int> = mutex.withLock {
        Triple(chapterToSmilMap.size, scannedSmilFiles.size, failedSmilFiles.size)
    }

    /**
     * Clears all index data.
     * Should be called when the reader session ends.
     */
    fun clear() {
        chapterToSmilMap.clear()
        scannedSmilFiles.clear()
        failedSmilFiles.clear()
    }
}

