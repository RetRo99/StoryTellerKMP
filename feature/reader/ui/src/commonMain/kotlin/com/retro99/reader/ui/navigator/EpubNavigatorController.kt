package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.audio.AudioController

/**
 * Controller interface for EPUB navigation and settings.
 *
 * This controller is owned by the View layer and handles:
 * - Page navigation (next/previous)
 * - Chapter navigation
 * - Reader settings application
 * - Media playback for ReadAloud books
 * - State observation via flows
 *
 * Platform implementations wrap the native navigator components:
 * - Android: [EpubNavigatorFragment] from Readium
 * - iOS: EPUBNavigatorViewController via bridge
 *
 * The View creates this controller after the publication is ready and
 * uses it to execute navigation commands from the ViewModel.
 */
interface EpubNavigatorController : EpubNavigatorControllerNew, AudioController

