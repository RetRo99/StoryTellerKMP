package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.audio.IosAudioController
import org.koin.core.annotation.Single

/**
 * iOS implementation of [EpubNavigatorController].
 * Uses interface delegation to separate Book and Audio concerns.
 *
 * @param bookController The controller handling navigation and book settings.
 * @param audioController The controller handling audio playback for ReadAloud books.
 */
@Single(
    binds = [
        BookController::class,
        AudioController::class,
    ]
)
class IosEpubNavigatorController(
    private val bookController: IosBookController,
    private val audioController: IosAudioController,
) : EpubNavigatorController,
    BookController by bookController,
    AudioController by audioController {

    /**
     * Initializes media overlays for ReadAloud books.
     * Delegates to the [audioController].
     */
    fun initializeMediaOverlaysIfNeeded() {
        audioController.initializeMediaOverlaysIfNeeded()
    }

    override fun close() {
        bookController.close()
        audioController.close()
    }
}
