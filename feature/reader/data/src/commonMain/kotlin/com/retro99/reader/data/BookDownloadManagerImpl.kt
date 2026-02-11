package com.retro99.reader.data

import com.retro99.reader.domain.BookDownloadManager

/**
 * Platform-specific implementation of [BookDownloadManager].
 *
 * Each platform implements this to handle downloads that survive app being killed:
 * - Android: Uses a ForegroundService with notification
 * - iOS: Uses URLSession with background configuration
 */
expect class BookDownloadManagerImpl : BookDownloadManager

