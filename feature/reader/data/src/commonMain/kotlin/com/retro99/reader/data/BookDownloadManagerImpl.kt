package com.retro99.reader.data

/**
 * Platform-specific implementation of BookDownloadManager.
 *
 * Each platform implements this to handle downloads that survive app being killed:
 * - Android: Uses a ForegroundService with notification
 * - iOS: Uses URLSession with background configuration
 *
 * Note: The actual implementations bind to BookDownloadManager interface via Koin's
 * @Single(binds = [BookDownloadManager::class]) annotation rather than declaring
 * the interface here, to avoid Kotlin Multiplatform metadata compilation issues.
 */
expect class BookDownloadManagerImpl

