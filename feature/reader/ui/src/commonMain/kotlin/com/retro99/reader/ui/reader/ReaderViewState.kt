package com.retro99.reader.ui.reader

import com.retro99.base.result.AppError
import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.model.ChapterInfo
import com.retro99.reader.ui.model.ChapterReadingTimeInfo
import com.retro99.reader.ui.model.PositionConflictUiModel
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.TocItemUiModel
import com.retro99.reader.ui.publication.PublicationState

data class ReaderViewState(
    val bookType: BookType,
    val bookUuid: String,
    val bookTitle: String = "",
    val bookCoverUrl: String? = null,
    val localFilePath: String? = null,
    val publicationState: PublicationState? = null,
    val positionConflict: PositionConflictUiModel? = null,
    val isSettingsVisible: Boolean = false,
    val error: AppError? = null,
    // Current time formatted according to user's locale (updated every minute)
    val currentTime: String = "",
    // Media playback state for ReadAloud books
    val isPlaying: Boolean = false,
    val currentAudioPositionMs: Long = 0L,
    val totalDurationMs: Long? = null,
    // Whether the media player is ready (for ReadAloud books)
    val isAudioPlayerReady: Boolean = false,
    // Table of contents
    val tableOfContents: List<TocItemUiModel> = emptyList(),
    val isTocVisible: Boolean = false,
    // TOC navigation undo - stores the position before navigating to a chapter
    val previousTocPosition: PositionUiModel? = null,
    // Current chapter info (page position and word count) based on actual viewport display
    val chapterInfo: ChapterInfo? = null,
    // Estimated reading time for the current chapter
    val chapterReadingTimeInfo: ChapterReadingTimeInfo? = null,
    // Flag to show snackbar when ReadAloud book has no media overlays
    val showNoAudioMessage: Boolean = false,
) {
    /**
     * Whether this is a ReadAloud book with media overlay support.
     * Both conditions must be true: the book type must be READALOUD and
     * the publication must have media overlays.
     */
    val isReadAloud: Boolean
        get() = bookType == BookType.READALOUD && publicationState?.publication?.hasMediaOverlays == true

    /**
     * Convenience accessor for the current reader settings.
     * Returns null if no publication is loaded.
     */
    val currentSettings get() = publicationState?.settings

    /**
     * Convenience accessor for the current reading position.
     * Returns null if no publication is loaded or position is not set.
     */
    val currentPosition get() = publicationState?.position
}

