package com.retro99.reader.ui.reader

import com.retro99.base.ui.BaseIntent
import com.retro99.reader.domain.model.ReaderSettingsDomainModel

sealed interface ReaderIntent : BaseIntent {
    data class UpdateProgress(
        val locator: String,
        val progression: Float,
    ) : ReaderIntent

    data class UpdateSettings(
        val settings: ReaderSettingsDomainModel,
    ) : ReaderIntent

    data object ToggleSettings : ReaderIntent

    data object Close : ReaderIntent

    data object GoToNextPage : ReaderIntent

    data object GoToPreviousPage : ReaderIntent
}

