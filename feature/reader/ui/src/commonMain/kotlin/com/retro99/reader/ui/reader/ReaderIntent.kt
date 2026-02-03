package com.retro99.reader.ui.reader

import com.retro99.base.ui.BaseIntent
import com.retro99.books.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel

sealed interface ReaderIntent : BaseIntent {
    data class UpdatePosition(
        val position: PositionUiModel,
    ) : ReaderIntent

    data class UpdateSettings(
        val settings: ReaderSettingsUiModel,
    ) : ReaderIntent

    data object ToggleSettings : ReaderIntent

    data object Close : ReaderIntent
}

