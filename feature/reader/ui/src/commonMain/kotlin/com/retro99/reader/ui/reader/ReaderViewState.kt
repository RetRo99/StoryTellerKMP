package com.retro99.reader.ui.reader

import com.retro99.base.result.AppError
import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.model.ChapterPageInfo
import com.retro99.reader.ui.model.ChapterReadingTimeInfo
import com.retro99.reader.ui.model.PositionConflictUiModel
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.model.TocItemUiModel
import com.retro99.reader.ui.publication.EpubPublication

data class ReaderViewState(
    val bookType: BookType,
    val bookUuid: String,
    val bookTitle: String = "",
    val bookCoverUrl: String? = null,
    val localFilePath: String? = null,
    val publication: EpubPublication? = null,
    val positionConflict: PositionConflictUiModel? = null,
    val isSettingsVisible: Boolean = false,
    val error: AppError? = null,
    // Current reader settings (updated when user changes settings)
    val currentSettings: ReaderSettingsUiModel? = null,
    // Current time formatted according to user's locale (updated every minute)
    val currentTime: String = "",
    // Media playback state for ReadAloud books
    val isPlaying: Boolean = false,
    val currentAudioPositionMs: Long = 0L,
    val totalDurationMs: Long? = null,
    val playbackSpeed: Float = 1.0f,
    // Last known text position (used for saving audio position)
    val lastKnownPosition: PositionUiModel? = null,
    // Whether the media player is ready (for ReadAloud books)
    val isAudioPlayerReady: Boolean = false,
    // Table of contents
    val tableOfContents: List<TocItemUiModel> = emptyList(),
    val isTocVisible: Boolean = false,
    // TOC navigation undo - stores the position before navigating to a chapter
    val previousTocPosition: PositionUiModel? = null,
    // Current page within the chapter based on actual viewport display
    val chapterPageInfo: ChapterPageInfo? = null,
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
        get() = bookType == BookType.READALOUD && publication?.hasMediaOverlays == true
}

