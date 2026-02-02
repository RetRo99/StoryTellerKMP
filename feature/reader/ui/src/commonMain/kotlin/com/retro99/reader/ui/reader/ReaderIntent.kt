package com.retro99.reader.ui.reader

import com.retro99.base.ui.BaseIntent
import com.retro99.reader.ui.model.ReaderSettingsUiModel

sealed interface ReaderIntent : BaseIntent {
    data class UpdateProgress(
        val locatorHref: String?,
        val locatorType: String?,
        val locatorTitle: String?,
        val progression: Double?,
        val totalProgression: Double?,
        val chapterIndex: Int?,
        val totalChapters: Int?,
    ) : ReaderIntent

    data class UpdateSettings(
        val settings: ReaderSettingsUiModel,
    ) : ReaderIntent

    data object ToggleSettings : ReaderIntent

    data object Close : ReaderIntent
}

