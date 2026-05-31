package com.retro99.reader.ui.playback

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.di.ReaderScope
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

private const val TAG = "čič123"

/**
 * Manages metadata for audio playback in the reader.
 *
 * ## Architecture (Post-Refactoring)
 *
 * The [MediaPlaybackService] now owns the ExoPlayer and MediaLibrarySession.
 * This class is a thin client that:
 * - Stores metadata locally for building MediaItems
 * - Delegates metadata/book info updates to the service
 *
 * The service handles:
 * - MediaSession creation and callbacks
 * - Seek commands from headphones/car
 * - Deep link PendingIntent creation
 * - Audio focus (via ExoPlayer's handleAudioFocus=true)
 */
@OptIn(UnstableApi::class)
@Scope(ReaderScope::class)
@Scoped
class MediaSessionManager(
    private val controller: MediaPlaybackController,
) {
    init {
        Log.d(TAG, "MediaSessionManager CREATED (new instance)")
    }
    private var bookTitle: String = "Reading Aloud"
    private var chapterTitle: String? = null
    private var coverArtwork: ByteArray? = null

    // Book identification for deep link navigation
    private var serverId: String? = null
    private var bookUuid: String? = null
    private var bookType: BookType? = null

    /**
     * Initializes the manager.
     * The service already owns the MediaSession, so we don't create one here.
     */
    fun initialize() {
        Log.d(TAG, "MediaSessionManager.initialize() called")
    }

    /**
     * Sets the book identification for deep link navigation.
     * Delegates to the service for PendingIntent updates and to the controller
     * for tracking which book is playing.
     */
    fun setBookInfo(serverId: String, bookUuid: String, bookType: BookType) {
        Log.d(TAG, "MediaSessionManager.setBookInfo(bookUuid=$bookUuid)")
        this.serverId = serverId
        this.bookUuid = bookUuid
        this.bookType = bookType

        // Update the service metadata for deep links
        controller.serviceInstance?.updateMetadata(
            serverId = serverId,
            bookUuid = bookUuid,
            bookType = bookType,
        )

        // Track which book is playing for reconnection
        controller.setCurrentPlayingBook(serverId, bookUuid, bookType)
    }

    /**
     * Updates the stored metadata for notifications and lockscreen.
     * Also updates the service's metadata.
     *
     * @param bookTitle The title of the book
     * @param chapterTitle Optional chapter title
     * @param coverArtwork Optional cover image as PNG byte array. Pass null to keep existing.
     */
    fun updateMetadata(
        bookTitle: String,
        chapterTitle: String? = null,
        coverArtwork: ByteArray? = null,
    ) {
        this.bookTitle = bookTitle
        this.chapterTitle = chapterTitle
        if (coverArtwork != null) {
            this.coverArtwork = coverArtwork
        }

        // Update the service metadata
        controller.serviceInstance?.updateMetadata(
            bookTitle = bookTitle,
            chapterTitle = chapterTitle,
            coverArtwork = coverArtwork,
        )
    }

    /**
     * Builds the current MediaMetadata based on stored book/chapter titles and cover.
     * Use this when creating a new MediaItem to ensure proper notification display.
     */
    fun buildCurrentMetadata(): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(chapterTitle ?: bookTitle)
            .setArtist(if (chapterTitle != null) bookTitle else "Parrot")
            .setDisplayTitle(chapterTitle ?: bookTitle)
            .apply {
                setArtworkDataIfSmall(coverArtwork, TAG)
            }
            .build()
    }

    /**
     * Releases resources. No longer needs to release MediaSession since
     * the service owns it.
     */
    fun release() {
        Log.d(TAG, "MediaSessionManager.release() called")
    }
}

