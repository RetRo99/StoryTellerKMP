package com.retro99.reader.ui.navigator

import org.koin.core.annotation.Single

/**
 * iOS implementation of [EpubNavigatorController].
 * Delegates book navigation to [IosBookController].
 *
 * Note: AudioController is now created separately via Koin Factory with EpubPublication
 * as a parameter, so it's no longer part of this controller.
 *
 * @param bookController The controller handling navigation and book settings.
 */
@Single(binds = [BookController::class])
class IosEpubNavigatorController(
    private val bookController: IosBookController,
) : EpubNavigatorController,
    BookController by bookController {

    override fun close() {
        bookController.close()
    }
}
